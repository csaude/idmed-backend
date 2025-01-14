package mz.org.fgh.sifmoz.backend.form

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class Form extends BaseEntity {
    String id
    String code
    String description
    String unit
    String howToUse

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Form_Idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code nullable: false, unique: true
        description nullable: false, blank: false
        unit nullable: true, blank: true
        howToUse nullable: true, blank: true
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(stockMenuCode,dashboardMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
