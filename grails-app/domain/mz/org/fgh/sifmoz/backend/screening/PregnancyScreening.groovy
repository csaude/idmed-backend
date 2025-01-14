package mz.org.fgh.sifmoz.backend.screening

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class PregnancyScreening extends BaseEntity {
    String id
    boolean pregnant
    boolean menstruationLastTwoMonths
    Date lastMenstruation
    @JsonBackReference
    PatientVisit visit
    Clinic clinic
    String origin
    static belongsTo = [PatientVisit]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PregnancyScreening_Idx'
    }

    static constraints = {
        lastMenstruation(nullable: true, blank: true)
        clinic blank: true, nullable: true
        origin nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
            clinic = Clinic.findWhere(mainClinic: true)
        }
    }

    @Override
    public String toString() {
        return "PregnancyScreening{" +
                "pregnant=" + pregnant +
                ", menstruationLastTwoMonths=" + menstruationLastTwoMonths +
                ", childDeliveryPrevision=" + lastMenstruation +
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
