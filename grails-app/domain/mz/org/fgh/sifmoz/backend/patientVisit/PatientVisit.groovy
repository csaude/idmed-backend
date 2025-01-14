package mz.org.fgh.sifmoz.backend.patientVisit

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import com.fasterxml.jackson.annotation.JsonView
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.screening.AdherenceScreening
import mz.org.fgh.sifmoz.backend.screening.PregnancyScreening
import mz.org.fgh.sifmoz.backend.screening.RAMScreening
import mz.org.fgh.sifmoz.backend.screening.TBScreening
import mz.org.fgh.sifmoz.backend.screening.VitalSignsScreening
import org.springframework.cglib.proxy.Mixin

import javax.persistence.Table

class PatientVisit extends BaseEntity {
    String id
    Date visitDate
    @JsonIgnore
    Clinic clinic
    Date creationDate = new Date()
    String origin

    @JsonIgnore
    Patient patient
    static belongsTo = [Patient]

    @JsonManagedReference
    static hasMany = [patientVisitDetails: PatientVisitDetails, adherenceScreenings: AdherenceScreening, vitalSignsScreenings: VitalSignsScreening,
                      pregnancyScreenings : PregnancyScreening, tbScreenings: TBScreening, ramScreenings: RAMScreening]

    static mapping = {
       id generator: "assigned"
       id column: 'id', index: 'Pk_patientVisit_Idx'
    }

    static constraints = {
        creationDate nullable: true
        origin nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode))
        }
        return menus
    }
}
