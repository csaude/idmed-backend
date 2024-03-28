package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class Localidade extends BaseEntity {
    String id
    String code
    String description

    static belongsTo = [postoAdministrativo: PostoAdministrativo, district: District]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Localidade_Idx'
    }
    static constraints = {
        code nullable: false, unique: true
        description nullable: false
        postoAdministrativo nullable: true
        district nullable: true
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,stockMenuCode,dashboardMenuCode,administrationMenuCode))
        }
        return menus
    }
}
