package mz.org.fgh.sifmoz.backend.openmrsErrorLog

class OpenmrsErrorLog  {

    String id
    String patient
    String nid
    String servicoClinico
    String patientVisitDetails
    Date dateCreated = new Date()
    Date pickupDate
    Date  returnPickupDate
    String errorDescription
    String jsonRequest

    static constraints = {
        returnPickupDate nullable: true, blank: true
        errorDescription maxSize: 15000
        jsonRequest maxSize: 15000
    }

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_OpenmrsErrorLog_Idx'
        errorDescription type: 'text'
    }

    def beforeInsert() {
        dateCreated = new Date()
        if (!id) {
            id = UUID.randomUUID()
        }
    }

}
