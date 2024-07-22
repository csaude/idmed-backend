package mz.org.fgh.sifmoz.backend.screening

import com.fasterxml.jackson.annotation.JsonBackReference
import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu

class TBScreening extends BaseEntity {
    String id
    boolean parentTBTreatment
    boolean cough
    boolean fever
    boolean losingWeight
    boolean treatmentTB
    boolean treatmentTPI
  //  boolean referedToUSTB
    Date startTreatmentDate
    boolean fatigueOrTirednessLastTwoWeeks
    boolean sweating

    @JsonBackReference
    PatientVisit visit
    Clinic clinic

    static belongsTo = [PatientVisit]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_TBScreening_Idx'
    }

    static constraints = {
        startTreatmentDate(nullable: true, blank: true)
        visit(nullable: true, blank: true)
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
        return "TBScreening{" +
                "parentTBTreatment=" + parentTBTreatment +
                ", cough=" + cough +
                ", fever=" + fever +
                ", losingWeight=" + losingWeight +
                ", treatmentTB=" + treatmentTB +
                ", treatmentTPI=" + treatmentTPI +
                ", startTreatmentDate=" + startTreatmentDate +
                ", fatigueOrTirednessLastTwoWeeks=" + fatigueOrTirednessLastTwoWeeks +
                ", sweating=" + sweating +
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
