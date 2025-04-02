package mz.org.fgh.sifmoz.backend.interoperabilityTransationLog

class InteroperabilityTransationLog {

    String id
    String messageId
    String queueName
    String sourceName
    String payloadRequest
    String payloadResponse
    String stage
    String status
    String errorMessage
    Date dateCreated
    Date lastUpdated = new Date()

    static constraints = {
        messageId nullable: false, maxSize: 255
        queueName nullable: false, maxSize: 255
        sourceName nullable: false, maxSize: 255
        payloadRequest nullable: false
        payloadResponse nullable: true
        status nullable: false, inList: ['COMPLETED', 'FAILED']
        stage nullable: false, inList: ['RECEIVED', 'PROCESSED', 'SENT']
        errorMessage nullable: true
    }

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'pk_interoperability_log_idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }
}
