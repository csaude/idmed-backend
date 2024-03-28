package mz.org.fgh.sifmoz.backend.patient

import mz.org.fgh.sifmoz.backend.appointment.Appointment
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Localidade
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.PostoAdministrativo
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.group.GroupInfo
import mz.org.fgh.sifmoz.backend.groupMember.GroupMember
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.patientAttribute.PatientAttribute
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReference
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class Patient extends BaseEntity {
    String id
    String firstNames
    String middleNames
    String lastNames
    String gender
    Date dateOfBirth
    String cellphone
    String alternativeCellphone
    String address
    String addressReference
    boolean accountstatus
    String hisUuid
    String hisLocation
    String hisLocationName
    HealthInformationSystem his
    Province province
    Localidade bairro
    District district
    PostoAdministrativo postoAdministrativo
    char hisSyncStatus
    String hisProvider
    Long matchId

    Clinic clinic
    Date creationDate = new Date()
    static belongsTo = [Clinic]

    static hasMany = [
            attributes           : PatientAttribute,
            identifiers          : PatientServiceIdentifier,
            appointments         : Appointment,
            patientTransReference: PatientTransReference
    ]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Patient_Idx'
    }

    static constraints = {
        dateOfBirth(nullable: true, blank: true, validator: { dateofbirth, urc ->
            return dateofbirth != null ? dateofbirth <= new Date() : null
        })
        middleNames nullable: true
        cellphone(nullable: true )
        alternativeCellphone(nullable: true, maxSize: 12, minSize: 9)
        address nullable: true, maxSize: 750
        addressReference nullable: true, maxSize: 750
        province nullable: false
        bairro nullable: true
        postoAdministrativo nullable: true
        clinic nullable: false
        hisUuid nullable: true
        his nullable: true
        hisLocation nullable: true
        hisLocationName nullable: true
        creationDate nullable: true
        hisSyncStatus nullable: true
        hisProvider nullable: true
        matchId nullable: false, unique: true
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
        if (matchId == null) {
            def  patient =  findAll( [sort: ['matchId': 'desc']])
            if (patient.size() == 0) {
                matchId = 1
            } else {
                matchId = patient.get(0).matchId + 1
            }

        }
    }
    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode))
        }
        return menus
    }

    boolean isActiveOnGroup(String serviceCode , Set <GroupInfo> patientActiveGroups)  {
        if (!Utilities.listHasElements(patientActiveGroups as ArrayList<?>)) return false
        for (GroupInfo groupInfo : patientActiveGroups) {
            if (groupInfo?.service?.code == serviceCode && groupInfo?.isActive()) return true
        }
        return false
    }
}
