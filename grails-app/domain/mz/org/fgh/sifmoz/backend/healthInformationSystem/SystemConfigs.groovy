package mz.org.fgh.sifmoz.backend.healthInformationSystem

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class SystemConfigs extends BaseEntity{

    String id
    String key
    String value
    String description

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_SystemConfigs_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        key nullable: false, unique: true
        value nullable: false
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
           // menus = Menu.findAllByCodeInList(Arrays.asList(administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
