package mz.org.fgh.sifmoz.backend.patientUpdateOpenMrsErrorLog

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientUpdateOpenMrsErrorLog  extends BaseEntity{

    String id
    String patient
    String nid
    String servicoClinico
    Date dateCreated = new Date()
    String errorDescription

    
    static constraints = {
        errorDescription maxSize: 15000
    }

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientOpenmrsErrorLog_Idx'
        errorDescription type: 'text'
    }

    def beforeInsert() {
        dateCreated = new Date()
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,reportsMenuCode))
        }
        return menus
    }
}
