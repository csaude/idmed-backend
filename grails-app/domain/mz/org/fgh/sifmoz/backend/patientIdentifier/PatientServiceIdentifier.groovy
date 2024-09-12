package mz.org.fgh.sifmoz.backend.patientIdentifier

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class PatientServiceIdentifier extends BaseEntity {
    String id
    Date startDate
    Date endDate
    Date reopenDate
    String value
    String state
    boolean prefered
    IdentifierType identifierType
    ClinicalService service
    Clinic clinic
    String origin

    static belongsTo = [patient: Patient]

    static hasMany = [episodes: Episode]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientServiceIdentifier_Idx'
    }

    static constraints = {
        value(unique: ['patient', 'service'], nullable: true)
        startDate(nullable: false, blank: false, validator: { startDate, urc ->
            return startDate != null ? startDate <= new Date() : null
        })
        endDate nullable: true
        reopenDate nullable: true
        origin nullable: true
    }

    boolean hasEpisodes () {
        return Utilities.listHasElements(this.episodes as ArrayList<?>)
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
