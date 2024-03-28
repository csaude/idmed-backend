package mz.org.fgh.sifmoz.backend.groupType

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class GroupType extends BaseEntity {
    String id
    String code
    String description

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_GroupType_Idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code nullable: false, unique: true
        description nullable: false
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(groupsMenuCode,homeMenuCode))
        }
        return menus
    }
}
