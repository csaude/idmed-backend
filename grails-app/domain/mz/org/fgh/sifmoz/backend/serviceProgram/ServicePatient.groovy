package mz.org.fgh.sifmoz.backend.serviceProgram

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason

class ServicePatient {

    Date startDate
    Date stopDate
    Date reopenDate
    StartStopReason startReason
    StartStopReason stopReason
    String startNotes
    String stopNotes
    ClinicalService clinicalService
    boolean prefered

    static belongsTo = [patient: Patient]

    static mapping = {
        datasource 'ALL'
    }
    static constraints = {
        startReason nullable: false
        startNotes nullable: false

        stopReason nullable: true
        stopNotes nullable: true

        startDate(nullable: false, blank: false, validator: { startDate, urc ->
            return startDate <= new Date()
        })
        stopDate(nullable: true, blank: true, validator: { stopDate, urc ->
            return stopDate != null ? startDate < stopDate : null
        })
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }


}
