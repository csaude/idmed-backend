package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class AbsentPatientsReport extends BaseEntity{
    String id //ReportId
    String reportId
    String clinic
    String clinicalServiceId
    String provinceId
    String districtId
    String periodType
    String period
    Date startDate
    Date endDate
    String nid
    String name
    Integer idade
    String address
    String servedService
    Date dateBackUs
    String contact
    Date dateMissedPickUp
    Date dateIdentifiedAbandonment
    Date returnedPickUp

    static constraints = {
        id generator: "uuid"
        clinic nullable: true
        provinceId nullable: true
        districtId nullable: true
        periodType nullable: false , inList: ['SPECIFIC','MONTH','QUARTER','SEMESTER','ANNUAL']
        period nullable: true
        startDate nullable: true
        idade nullable: true
        address nullable: true, type: 'text' // Alteração para aceitar texto longo
        servedService nullable: true
        endDate nullable: true
        dateBackUs nullable: true
        contact nullable: true
        dateMissedPickUp nullable: true
        dateIdentifiedAbandonment nullable: true
        returnedPickUp nullable: true
    }

    static mapping = {
        datasource 'ALL'
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
