package mz.org.fgh.sifmoz.backend.interoperabilityTransationLog

import grails.gorm.transactions.Transactional

@Transactional
class InteroperabilityTransationLogService {

    void saveInteroperabilityTransactionLog(String messageId, String queueName, String sourceName,
                                            def payloadRequest, def payloadResponse, String stage,
                                            String status, String errorMessage) {
        try {
            InteroperabilityTransationLog interoperabilityTransationLog = InteroperabilityTransationLog.findWhere(messageId: messageId)
            if (!interoperabilityTransationLog){
                interoperabilityTransationLog = new InteroperabilityTransationLog()
                interoperabilityTransationLog.beforeInsert()
            }
            interoperabilityTransationLog.messageId = messageId
            interoperabilityTransationLog.queueName = queueName
            interoperabilityTransationLog.sourceName = sourceName
            interoperabilityTransationLog.payloadRequest = payloadRequest
            interoperabilityTransationLog.payloadResponse = payloadResponse
            interoperabilityTransationLog.status = stage
            interoperabilityTransationLog.status = status
            interoperabilityTransationLog.errorMessage = errorMessage
            interoperabilityTransationLog.save(flush: true, failOnError: true)
        } catch (Exception e) {
            e.printStackTrace()
        }

    }
}
