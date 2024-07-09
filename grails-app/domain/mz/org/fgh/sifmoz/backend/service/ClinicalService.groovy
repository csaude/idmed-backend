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

    String id
    String code
    String description
    IdentifierType identifierType
    boolean active

    static belongsTo = [ClinicalServiceAttributeType]
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

    boolean isPrep() {
        return this.code == "PREP"
    }

    boolean isTarv() {
        return this.code == "TARV"
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,administrationMenuCode,reportsMenuCode,homeMenuCode))
        }
        return menus
    }
}
