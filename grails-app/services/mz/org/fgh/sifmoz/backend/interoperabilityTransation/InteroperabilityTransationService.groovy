package mz.org.fgh.sifmoz.backend.interoperabilityTransation

import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.interoperabilityTransationLog.InteroperabilityTransationLog
import mz.org.fgh.sifmoz.backend.interoperabilityTransationLog.InteroperabilityTransationLogService
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.prescription.PrescriptionService
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@EnableScheduling
@Transactional
class InteroperabilityTransationService {

    JmsTemplate jmsTemplate
    InteroperabilityTransationLogService interoperabilityTransationLogService
    PrescriptionService prescriptionService
    IPatientService patientService

    static final String ACTIVEMQ_PRESCRIPTION_QUEUE = "prescription.queue"
    static final String ACTIVEMQ_PRESCRIPTION_RESPONSE_QUEUE = "prescription.response.queue"
    static final String ACTIVEMQ_DISPENSE_QUEUE = "dispensation.queue"
    static final String ACTIVEMQ_PATIENT_SYNC = "patient.sync.queue"
    static final String ACTIVEMQ_STAGE_RECEIVED = "RECEIVED"
    static final String ACTIVEMQ_STAGE_PROCESSED = "PROCESSED"
    static final String ACTIVEMQ_STAGE_READY_TO_SEND = "READY_TO_SEND"

    static final String ACTIVEMQ_STATUS_FAILED = "FAILED"
    static final String ACTIVEMQ_STATUS_COMPLETED = "COMPLETED"

    static final String ACTIVEMQ_ERROR_MESSAGE_PATIENT_NOT_FOUND = "PACIENTE NAO EXISTE NO IDMED"

    static final String SOURCEPOC = "POC"

    static lazyInit = false

    @JmsListener(destination = ACTIVEMQ_PRESCRIPTION_QUEUE)
    void loadPOCMessage(String message) {
        String messageId = UUID.randomUUID().toString()
        println "🔹 Mensagem recebida: ${messageId}"
        loadMessageFromPOCToiDMED(messageId, message)
    }

    void sendMessageToPOC(def messageId, def message, def queueName) {
        def objectJSON = new JsonSlurper().parseText(message)
        try {
            println " ======= PREPARANDO O ENVIO PARA POC NA FILA ${queueName} ======"
            jmsTemplate.convertAndSend(queueName, message as String)
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_DISPENSE_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_READY_TO_SEND, ACTIVEMQ_STATUS_COMPLETED, null)
            prescriptionService.updatePOCPrescriptionLog(messageId)
            println "✅ Pedido ${messageId} enviado para a fila ${queueName} com status ${ACTIVEMQ_STATUS_COMPLETED}"
        } catch (Exception e) {
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_DISPENSE_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_READY_TO_SEND, ACTIVEMQ_STATUS_FAILED, e.message)
            println "❌ Erro ao processar mensagem: ${e.message}"
        }
    }

    void loadMessageFromPOCToiDMED(def messageId, def message) {
        if (isJson(message)) {
            try {
                def objectJSON = new JsonSlurper().parseText(message)

                Patient patient = Patient.findWhere(hisUuid: objectJSON.patient_uuid)
                if (patient) {
                    interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_COMPLETED, null)
                    prescriptionService.savePrescriptionFromPOC(objectJSON, messageId, patient)
                    Thread.sleep(1000)
                    interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_PROCESSED, ACTIVEMQ_STATUS_COMPLETED, null)
                    sendMessageToPOC(messageId, "A prescricao do paciente ${patient.firstNames} ${patient.lastNames} criado com sucesso", ACTIVEMQ_PRESCRIPTION_RESPONSE_QUEUE)
                } else {
                    interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, ACTIVEMQ_ERROR_MESSAGE_PATIENT_NOT_FOUND)
                }
            } catch (Exception e) {
                println "❌ Erro ao processar mensagem: ${e.message}"
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION_QUEUE, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, e.message)
            }
        }
    }

    void activeMQLoadNotProcessedMessages() {
        println " - ACTIVE MQ - NOT PROCESSED IN IDMED" + new Date()
        List<InteroperabilityTransationLog> interoperabilityReceivedNotProcessedTransationLogs = InteroperabilityTransationLog.findAllByStageAndStatus(ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED)
        for (InteroperabilityTransationLog interoperabilityTransationLog : interoperabilityReceivedNotProcessedTransationLogs) {
            try {
                loadMessageFromPOCToiDMED(interoperabilityTransationLog.messageId, interoperabilityTransationLog.payloadRequest)
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                continue
            }
        }
    }

    void activeMQLoadNotSentMessages() {
        println " - ACTIVE MQ - NOT SENT TO POC " + new Date()
        List<InteroperabilityTransationLog> interoperabilityProcessedNotSetTransationLogs = InteroperabilityTransationLog.findAllByStageAndStatus(ACTIVEMQ_STAGE_READY_TO_SEND, ACTIVEMQ_STATUS_FAILED)
        for (InteroperabilityTransationLog interoperabilityTransationLog : interoperabilityProcessedNotSetTransationLogs) {
            try {
                sendMessageToPOC(interoperabilityTransationLog.messageId, interoperabilityTransationLog.payloadResponse, ACTIVEMQ_DISPENSE_QUEUE)
            } catch (Exception e) {
                e.printStackTrace()
            } finally {
                continue
            }

    @JmsListener(destination = ACTIVEMQ_PATIENT_SYNC)
    void loadPatientPocMessage(String message) {
        String messageId = UUID.randomUUID().toString()
        def objectJSON = new JsonSlurper().parseText(message)
        try {
            println "🔹 Mensagem recebida: ${message}"
            Patient patient = Patient.findWhere(hisUuid: objectJSON.patientUuid)
            if(!patient){
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PATIENT_SYNC, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_COMPLETED, null )
                patientService.savePatientFromPoc(objectJSON)
                Thread.sleep(1000)
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PATIENT_SYNC, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_PROCESSED, ACTIVEMQ_STATUS_COMPLETED, null )
            }else{
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PATIENT_SYNC, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, ACTIVEMQ_ERROR_MESSAGE_PATIENT_ALREADY_EXISTS )
            }
        } catch (Exception e) {
            println "❌ Erro ao processar mensagem: ${e.message}"
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PATIENT_SYNC, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, e.message )
        }
    }


    void sendMessageToPOC(String message, String messageId) {
        def objectJSON = new JsonSlurper().parseText(message)
        try{
            // Enviar mensagem para a fila existente no ActiveMQ remoto
            jmsTemplate.convertAndSend(ACTIVEMQ_DISPENSE, message as String)
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC,objectJSON, null, ACTIVEMQ_STAGE_READY_TO_SEND, ACTIVEMQ_STATUS_COMPLETED, null )
            prescriptionService.updatePOCPrescriptionLog(messageId)
            println "✅ Pedido ${messageId} enviado para a fila ${ACTIVEMQ_DISPENSE} com status ${ACTIVEMQ_STATUS_COMPLETED}"
        }catch (Exception e){
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC,objectJSON, null, ACTIVEMQ_STAGE_READY_TO_SEND, ACTIVEMQ_STATUS_COMPLETED, e.message )
            println "❌ Erro ao processar mensagem: ${e.message}"
        }
    }

    boolean isJson(String input) {
        try {
            new JsonSlurper().parseText(input)
            return true
        } catch (Exception e) {
            return false
        }
    }

    @Scheduled(fixedDelay = 180000L)
    void schedulerActiveMQRunning() {
        activeMQLoadNotProcessedMessages()
        activeMQLoadNotSentMessages()
    }

    void sendPrescriptionQueueResponse(String uuid, String status, String remoteId, String errorMessage) {
        def payload = [
                prescriptionUuid: uuid,
                remoteId       : remoteId,
                status         : status,
                errorMessage   : status == "ERROR" ? errorMessage : ""
        ]

        String jsonMessage = new JsonBuilder(payload).toString()

        jmsTemplate.convertAndSend(ACTIVEMQ_PRESCRIPTION_RESPONSE, jsonMessage)
    }
}
