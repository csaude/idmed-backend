package mz.org.fgh.sifmoz.backend.prescriptionDrug

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.protection.Menu

class PrescribedDrug extends BaseEntity {
    String id
    double amtPerTime
    int timesPerDay
    int prescribedQty
    String form
    boolean modified
    @JsonManagedReference
    Drug drug
    @JsonBackReference
    Prescription prescription
    Clinic clinic
    String origin
    static belongsTo = [Prescription]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PrescribedDrug_Idx'
    }
    static constraints = {
        timesPerDay(min: 1)
        clinic blank: true, nullable: true
        origin nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,administrationMenuCode))
        }
        return menus
    }
}
