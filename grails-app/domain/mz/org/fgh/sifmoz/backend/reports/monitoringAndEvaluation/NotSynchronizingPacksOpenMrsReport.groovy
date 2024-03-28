package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class NotSynchronizingPacksOpenMrsReport extends BaseEntity{

    String patient
    String nid
    String clinicalService
    String patientVisitDetails
    Date dateCreated = new Date()
    Date pickupDate
    Date returnPickupDate
    String errorDescription
    String jsonRequest
    String id //ReportId
    String reportId
    String pharmacyId
    int year
    Date startDate
    Date endDate
    String periodType
    String period
    String provinceId
    String districtId

    static mapping = {
        jsonRequest type: 'text'
    }

    static constraints = {
        id generator: "uuid"
        pharmacyId nullable: true
        provinceId nullable: true
        districtId nullable: true
        period nullable: true
        startDate nullable: true
        endDate nullable: true
        year nullable: true
        nid nullable: true
        clinicalService nullable: true
        dateCreated nullable: true
        returnPickupDate nullable: true
        errorDescription nullable: true
        jsonRequest nullable: true, size: 0..15000

    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(reportsMenuCode))
        }
        return menus
    }
}
