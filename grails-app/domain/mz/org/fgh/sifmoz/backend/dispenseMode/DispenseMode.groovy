package mz.org.fgh.sifmoz.backend.dispenseMode

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class DispenseMode extends BaseEntity {
    String id
    String code
    String description
    String openmrsUuid

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_DispenseMode_Idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
    }
    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,stockMenuCode,dashboardMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
