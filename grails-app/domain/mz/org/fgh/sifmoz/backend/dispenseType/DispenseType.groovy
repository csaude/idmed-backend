package mz.org.fgh.sifmoz.backend.dispenseType

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu

class DispenseType extends BaseEntity {
    public static final String DM = "DM"
    public static final String DT = "DT"
    public static final String DS = "DS"
    public static final String DA = "DA"
    String id
    String code
    String description

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_DispenseType_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code nullable: false, unique: true
        description nullable: false, blank: false

    }

    boolean isDM () {
        return this.code.equals(DM)
    }

    boolean isDT () {
        return this.code.equals(DT)
    }

    boolean isDS () {
        return this.code.equals(DS)
    }

    boolean isDA () {
        return this.code.equals(DA)
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
