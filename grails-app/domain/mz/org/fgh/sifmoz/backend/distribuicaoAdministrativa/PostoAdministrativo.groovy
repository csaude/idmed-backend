package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class PostoAdministrativo extends BaseEntity {
    String id
    String code
    String description
    static belongsTo = [district: District]
    static hasMany = [localidades: Localidade]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PostoAdministrativo_Idx'
    }
    static constraints = {
        code nullable: false, unique: true
        description nullable: false
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
