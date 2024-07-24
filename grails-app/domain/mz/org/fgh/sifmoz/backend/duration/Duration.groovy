package mz.org.fgh.sifmoz.backend.duration

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class Duration extends BaseEntity {

    String id
    int weeks
    String description

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Duration_Idx'
        datasource 'ALL'
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
