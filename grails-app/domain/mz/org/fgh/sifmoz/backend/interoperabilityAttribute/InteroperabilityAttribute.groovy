package mz.org.fgh.sifmoz.backend.interoperabilityAttribute

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.interoperabilityType.InteroperabilityType
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class InteroperabilityAttribute extends BaseEntity {

    String id
    @JsonManagedReference
    InteroperabilityType interoperabilityType
    String value

       static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_InteroperabilityAttribute_Idx'
           datasource 'ALL'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static belongsTo = [healthInformationSystem : HealthInformationSystem]

    static constraints = {
        value nullable: false, blank: false
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
