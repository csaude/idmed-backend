package mz.org.fgh.sifmoz.backend.tansreference

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientTransReferenceType extends BaseEntity {

    String id
    String description
    String code

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PatientTransReferenceType_Idx'
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }
    static constraints = {
        description(nullable: false, maxSize: 50, blank: false)
        code(nullable: false, maxSize: 50, blank: false, unique: true)
    }

    @Override
    public String toString() {
        return "PatientTransReferenceType{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", code='" + code + '\'' +
                '}';
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
