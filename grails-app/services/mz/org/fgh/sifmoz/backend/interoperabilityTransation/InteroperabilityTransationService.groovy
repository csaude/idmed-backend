package mz.org.fgh.sifmoz.backend.interoperabilityTransation

import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.interoperabilityTransationLog.InteroperabilityTransationLogService
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.prescription.PrescriptionService
import org.springframework.jms.annotation.JmsListener
import org.springframework.jms.core.JmsTemplate

@Transactional
class InteroperabilityTransationService {

    JmsTemplate jmsTemplate
    InteroperabilityTransationLogService interoperabilityTransationLogService
    PrescriptionService prescriptionService
    IPatientService patientService

     static final String ACTIVEMQ_PRESCRIPTION = "prescription.queue"
    static final String ACTIVEMQ_PRESCRIPTION_RESPONSE = "prescription.response.queue"
     static final String ACTIVEMQ_DISPENSE = "fila.dispenses"
    static final String ACTIVEMQ_PATIENT_SYNC = "patient.sync.queue"
     static final String ACTIVEMQ_STAGE_RECEIVED = "RECEIVED"
     static final String ACTIVEMQ_STAGE_PROCESSED = "PROCESSED"
     static final String ACTIVEMQ_STAGE_READY_TO_SEND = "READY_TO_SEND"

     static final String ACTIVEMQ_STATUS_FAILED = "FAILED"
     static final String ACTIVEMQ_STATUS_COMPLETED = "COMPLETED"

    static final String ACTIVEMQ_ERROR_MESSAGE_PATIENT_NOT_FOUND = "PACIENTE NAO EXISTE NO IDMED"

    static final String ACTIVEMQ_ERROR_MESSAGE_PATIENT_ALREADY_EXISTS = "PACIENTE JA EXISTE NO IDMED"

    static final String SOURCEPOC = "POC"

    @JmsListener(destination = ACTIVEMQ_PRESCRIPTION)
    void loadPOCMessage(String message) {
        String messageId = UUID.randomUUID().toString()
        def objectJSON = new JsonSlurper().parseText(message)
        try {
            println "🔹 Mensagem recebida: ${message}"
            Patient patient = Patient.findWhere(hisUuid: objectJSON.patient_uuid)
            if(patient){
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_COMPLETED, null )
                prescriptionService.savePrescriptionFromPOC(message, messageId, patient)
                Thread.sleep(1000)
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_PROCESSED, ACTIVEMQ_STATUS_COMPLETED, null )
            }else{
                interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, ACTIVEMQ_ERROR_MESSAGE_PATIENT_NOT_FOUND )
            }
        } catch (Exception e) {
            println "❌ Erro ao processar mensagem: ${e.message}"
            interoperabilityTransationLogService.saveInteroperabilityTransactionLog(messageId, ACTIVEMQ_PRESCRIPTION, SOURCEPOC, objectJSON, null, ACTIVEMQ_STAGE_RECEIVED, ACTIVEMQ_STATUS_FAILED, e.message )
        }
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
