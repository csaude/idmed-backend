package mz.org.fgh.sifmoz.backend.protection

import mz.org.fgh.sifmoz.backend.base.BaseEntity


class UiSection extends BaseEntity {

    String id
    String name
    String displayName
    String category
    String action
    String requestMapUrl
    Menu menu

    static hasMany = [roles: Role]
    static belongsTo = Role
    static mapping = {
        id generator: "assigned"
        roles joinTable: [name:"role_ui_sections",  key:"ui_section_id", column:"roles_id"]
    }
    static constraints = {
        name unique: true
        displayName nullable: true
        category nullable: true
        action nullable: true
        requestMapUrl nullable: true
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
