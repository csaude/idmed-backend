package mz.org.fgh.sifmoz.backend.provincialServer

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu


class ProvincialServer extends BaseEntity{

    String id
    String code
    String urlPath
    String port
    String destination
    String username
    String password

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_ProvincialServer_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        urlPath(nullable: false, blank: false)
        code(nullable: false, maxSize: 50, blank: false,unique: ['destination'])
        username(nullable: false,blank: false)
        password(nullable: false,blank: false)
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,administrationMenuCode,homeMenuCode))
        }
        return menus
    }

}
