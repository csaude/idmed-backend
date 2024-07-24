package mz.org.fgh.sifmoz.backend.prescriptionDetail

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescription.SpetialPrescriptionMotive
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.therapeuticLine.TherapeuticLine
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen

class PrescriptionDetail extends BaseEntity {
    String id
    String reasonForUpdate
    String reasonForUpdateDesc
    TherapeuticLine therapeuticLine
    TherapeuticRegimen therapeuticRegimen
    DispenseType dispenseType
    Prescription prescription
    SpetialPrescriptionMotive spetialPrescriptionMotive
    Date creationDate = new Date()
    Clinic clinic
    static belongsTo = [Prescription]

    static mapping = {
       id generator: "assigned"
       id column: 'id', index: 'Pk_PrescriptionDetail_Idx'
        datasource 'ALL'
    }

    static constraints = {
        reasonForUpdate nullable: true
        reasonForUpdateDesc nullable: true
        therapeuticRegimen nullable: true
        therapeuticLine nullable: true
        spetialPrescriptionMotive nullable: true
        creationDate nullable: true
        clinic blank: true, nullable: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
            clinic = Clinic.findWhere(mainClinic: true)
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
