package mz.org.fgh.sifmoz.backend.attributeType

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientAttributeType extends BaseEntity {
    String id
    String code
    String name
    String description
    String datatype

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientAttributeType_Idx'
        datasource 'ALL'
    }

    static constraints = {
        code nullable: false, unique: true
        name nullable: false, unique: true
        description nullable: true, blank: true
        datatype nullable: true, blank: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    List<Menu> hasMenus() {
        String patientMenuCode = '01'
        String groupsMenuCode = '02'
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode))
        }
        return menus
    }
}
