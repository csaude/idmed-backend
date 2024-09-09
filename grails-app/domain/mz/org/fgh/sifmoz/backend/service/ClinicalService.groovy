package mz.org.fgh.sifmoz.backend.service

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.serviceattribute.ClinicalServiceAttribute
import mz.org.fgh.sifmoz.backend.serviceattributetype.ClinicalServiceAttributeType
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen

class ClinicalService extends BaseEntity {
    static final String PREP = "PREP"
    static final String TARV = "TARV"
    static final String PPE = "PPE"
    static final String TPT = "TPT"
    static final String CCR = "CCR"

    String id
    String code
    String description
    IdentifierType identifierType
    boolean active

    static belongsTo = [ClinicalServiceAttributeType, TherapeuticRegimen]
    static hasMany = [clinicalServiceAttributes: ClinicalServiceAttributeType, therapeuticRegimens: TherapeuticRegimen, clinicSectors: ClinicSector]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_ClinicalService_Idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code nullable: false, unique: true
        description nullable: false
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode, groupsMenuCode, dashboardMenuCode, administrationMenuCode, reportsMenuCode, homeMenuCode))
        }
        return menus
    }

    boolean isPREP() {
        return this.code.equalsIgnoreCase(PREP)
    }

    boolean isTARV() {
        return this.code.equalsIgnoreCase(TARV)
    }

    boolean isPPE() {
        return this.code.equalsIgnoreCase(PPE)
    }

    boolean isTPT() {
        return this.code.equalsIgnoreCase(TPT)
    }

    boolean isCCR() {
        return this.code.equalsIgnoreCase(CCR)
    }
}