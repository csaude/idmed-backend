package mz.org.fgh.sifmoz.backend.clinic

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.nationalClinic.NationalClinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.protection.SecUser
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia.MmiaReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService

class Clinic extends BaseEntity {
    String id
    String code
    String notes
    String telephone
    String clinicName
    Province province
    District district
    FacilityType facilityType
    Boolean nationalClinic
    boolean mainClinic
    boolean active
    String uuid

    Clinic parentClinic
    String syncStatus
    String matchFC
    static belongsTo = [NationalClinic, SecUser]
    static hasMany = [sectors: ClinicSector, mmiaReports: MmiaReport, users: SecUser]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Clinic_Idx'
        mmiaReports joinTable: [name:"mmia_report_clinic", key:"clinic_id", column:"mmia_report_clinic_id"]
        users  joinTable: [name: "clinic_users", key: "clinic_id", column: "sec_user_id"]
        datasource 'ALL'
    }
    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code nullable: false
        notes nullable: true, blank: true
        telephone nullable: true, matches: /\d+/, maxSize: 12, minSize: 9
        clinicName nullable: false, unique: ['province','district']
       // sectors nullable: true
        nationalClinic nullable: true
        uuid unique: true
        parentClinic(nullable: true)
        matchFC nullable:  true
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Clinic clinic = (Clinic) o

        if (clinicName != clinic.clinicName) return false
        if (code != clinic.code) return false
        if (id != clinic.id) return false

        return true
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
           // menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,stockMenuCode,dashboardMenuCode,reportsMenuCode,administrationMenuCode))
        }
        return menus
    }
}
