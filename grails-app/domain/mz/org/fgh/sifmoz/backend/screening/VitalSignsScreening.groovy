package mz.org.fgh.sifmoz.backend.screening

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class VitalSignsScreening extends BaseEntity {
    String id
    int distort;
    String imc;
    double weight;
    int systole;
    double height;

    @JsonBackReference
    PatientVisit visit
    Clinic clinic

    static belongsTo = [PatientVisit]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_VitalSignsScreening_Idx'
    }

    static constraints = {
        distort (nullable: false)
        clinic nullable: false
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    public String toString() {
        return "VitalSignsScreening{" +
                "distort=" + distort +
                ", imc='" + imc + '\'' +
                ", weight=" + weight +
                ", systole=" + systole +
                ", height=" + height +
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
