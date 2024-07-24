package mz.org.fgh.sifmoz.backend.tansreference

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReferenceType

class PatientTransReference extends BaseEntity{

    String id
    Clinic origin
    String destination
    PatientServiceIdentifier identifier
    String syncStatus
    PatientTransReferenceType operationType
    Date operationDate
    Date creationDate
    Long matchId
    String patientStatus

    static belongsTo = [patient: Patient]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientTransReference_Idx'
        datasource 'ALL'
    }

    static constraints = {
        matchId nullable: true
        patientStatus nullable: true, blank: true
    }

    def beforeInsertId() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    def beforeInsert() {
        if (matchId == null) {
          matchId = identifier.patient.matchId
        }
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode))
        }
        return menus
    }
}
