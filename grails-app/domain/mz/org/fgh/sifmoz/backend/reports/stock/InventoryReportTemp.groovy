package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class InventoryReportTemp extends BaseEntity {
    String id //ReportId
    String reportId
    String pharmacyId
    String provinceId
    String districtId
    String period
    String periodType
    String month
    String quarter
    String semester
    int year
    Date startDate
    Date endDate
    String clinic
    String province
    String district

    String inventoryId
    String inventoryType
    String orderNumber
    Date captureDate
    Date inventoryStartDate
    Date inventoryEndDate
    String drugName
    String batchNumber
    Date expireDate
    Long balance
    Long adjustedValue
    String formDescription
    String notes
    String operation_type

    static mapping = {
        datasource 'ALL'
    }


    static constraints = {
        id generator: "uuid"
        pharmacyId nullable: true
        provinceId nullable: true
        districtId nullable: true
        period nullable: true
        month nullable: true
        quarter nullable: true
        semester nullable: true
        startDate nullable: true
        endDate nullable: true
        year nullable: true
        periodType nullable: true
        orderNumber nullable: true
        inventoryType nullable: true
        batchNumber nullable: true

        captureDate nullable: true
        inventoryStartDate nullable: true
        inventoryEndDate nullable: true
        drugName nullable: true
        expireDate nullable: true
        balance nullable: true
        adjustedValue nullable: true
        formDescription nullable: true
        notes nullable: true
        inventoryId nullable: true
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
