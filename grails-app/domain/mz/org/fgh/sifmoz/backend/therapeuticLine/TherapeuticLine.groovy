package mz.org.fgh.sifmoz.backend.therapeuticLine

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class TherapeuticLine extends BaseEntity {
    String id
    String code
    String description
    String uuid = UUID.randomUUID().toString()
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_TherapeuticLine_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code unique: true
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
