package mz.org.fgh.sifmoz.backend.screening

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class RAMScreening extends BaseEntity {
    String id
    String adverseReaction
    boolean adverseReactionMedicine
   // boolean referedToUSRam
    @JsonBackReference
    PatientVisit visit

    static belongsTo = [PatientVisit]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_RAMScreening_Idx'
    }

    static constraints = {
        adverseReaction(nullable: true, blank: true)
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    public String toString() {
        return "RAMScreening{" +
                "adverseReaction='" + adverseReaction + '\'' +
                ", adverseReactionMedicine=" + adverseReactionMedicine +
                '}';
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode))
        }
        return menus
    }
}
