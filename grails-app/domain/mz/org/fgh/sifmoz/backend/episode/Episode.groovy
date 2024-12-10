package mz.org.fgh.sifmoz.backend.episode


import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.protection.Menu
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
    String origin

    static belongsTo = [patientServiceIdentifier: PatientServiceIdentifier]

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
        origin nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,administrationMenuCode))
        }
        return menus
    }
}
