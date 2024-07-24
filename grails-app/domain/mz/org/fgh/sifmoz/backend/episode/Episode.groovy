package mz.org.fgh.sifmoz.backend.episode

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason

class Episode extends BaseEntity {
    String id
    Date episodeDate
    Date creationDate
    StartStopReason startStopReason
    String notes
    @JsonManagedReference
    EpisodeType episodeType
    @JsonManagedReference
    Clinic clinicSector
    @JsonIgnore
    Clinic clinic

    @JsonIgnore
    Clinic referralClinic

    boolean isAbandonmentDC

    static belongsTo = [patientServiceIdentifier: PatientServiceIdentifier]

//    @JsonBackReference
//    static hasMany = [patientVisitDetails: PatientVisitDetails]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_episode_Idx'
        datasource 'ALL'
    }

    static fetchMode = [patientVisitDetails: 'lazy']

    static constraints = {
        episodeDate(nullable: false, blank: false, validator: { episodeDate, urc ->
            return episodeDate <= new Date()
        })
        referralClinic nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
    }

//    @Override
//    public String toString() {
//        return "Episode{" +
//                "patientVisitDetails=" + patientVisitDetails +
//                ", patientServiceIdentifier=" + patientServiceIdentifier +
//                ", id='" + id + '\'' +
//                ", episodeDate=" + episodeDate +
//                ", creationDate=" + creationDate +
//                ", startStopReason=" + startStopReason +
//                ", notes='" + notes + '\'' +
//                ", episodeType=" + episodeType +
//                ", clinicSector=" + clinicSector +
//                ", clinic=" + clinic +
//                '}';
//    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,administrationMenuCode))
        }
        return menus
    }
}
