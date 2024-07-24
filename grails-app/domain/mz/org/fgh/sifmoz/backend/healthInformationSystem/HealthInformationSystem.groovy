package mz.org.fgh.sifmoz.backend.healthInformationSystem

import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.protection.Menu

class HealthInformationSystem extends BaseEntity {

    String id
    String abbreviation
    String description
    boolean active

    @JsonManagedReference
    static hasMany = [interoperabilityAttributes: InteroperabilityAttribute]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_HealthInformationSystem_Idx'
      //  interoperabilityAttributes (cascade: "all-delete-orphan")
        datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        abbreviation nullable: false, unique: true
        description nullable: false
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
