package mz.org.fgh.sifmoz.backend.pocPrescriptionLog

import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.service.ClinicalService

class PocPrescriptionLog {

    String id
    String messageId
    Prescription prescription
    Date prescriptionDate
    ClinicalService clinicalService
    Patient patient
    String nid
    String status
    Date dateCreated
    Date lastUpdated = new Date()

    static constraints = {
        messageId nullable: false, maxSize: 255
        prescription nullable: false
        prescriptionDate nullable: false
        clinicalService nullable: false
        patient nullable: false
        status nullable: false, inList: ['COMPLETED', 'PENDING']
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
