package mz.org.fgh.sifmoz.backend.screening

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class AdherenceScreening extends BaseEntity {
    String id
    boolean hasPatientCameCorrectDate
    int daysWithoutMedicine
    boolean patientForgotMedicine
    int lateDays
    String lateMotives

    @JsonBackReference
    PatientVisit visit
    Clinic clinic

    static belongsTo = [PatientVisit]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_AdherenceScreening_Idx'
    }

    static constraints = {
        lateMotives(nullable: true, maxSize: 1000)
        daysWithoutMedicine(nullable: true,blank: true)
        lateDays(nullable: true, blank: true)
        clinic blank: true, nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
            clinic = Clinic.findWhere(mainClinic: true)
        }
    }

    @Override
    public String toString() {
        return "AdherenceScreening{" +
                "hasPatientCameCorrectDate=" + hasPatientCameCorrectDate +
                ", daysWithoutMedicine=" + daysWithoutMedicine +
                ", patientForgotMedicine=" + patientForgotMedicine +
                ", lateDays=" + lateDays +
                ", lateMotives='" + lateMotives + '\'' +
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
