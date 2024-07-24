package mz.org.fgh.sifmoz.backend.patientAttribute


import mz.org.fgh.sifmoz.backend.attributeType.PatientAttributeType
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientAttribute extends BaseEntity {
    String id
    PatientAttributeType attributeType
    String value

    static belongsTo = [patient: Patient]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientAttribute_Idx'
        datasource 'ALL'
    }
    static constraints = {
        attributeType nullable: false
        value nullable: false, blank: false
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode))
        }
        return menus
    }
}
