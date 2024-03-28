package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class StockReportTemp extends BaseEntity{

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
    String orderNumber
    String drugName
    Date expiryDate
    Date dateReceived
    Long unitsReceived
    String manufacture
    String batchNumber


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
        manufacture nullable: true
        batchNumber nullable: true
    }


    @Override
    public String toString() {
        return "StockReportTemp{" +
                "id='" + id + '\'' +
                ", reportId='" + reportId + '\'' +
                ", pharmacyId='" + pharmacyId + '\'' +
                ", provinceId='" + provinceId + '\'' +
                ", districtId='" + districtId + '\'' +
                ", period='" + period + '\'' +
                ", periodType='" + periodType + '\'' +
                ", month='" + month + '\'' +
                ", quarter='" + quarter + '\'' +
                ", semester='" + semester + '\'' +
                ", year=" + year +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", orderNumber='" + orderNumber + '\'' +
                ", drugName='" + drugName + '\'' +
                ", expiryDate=" + expiryDate +
                ", dateReceived=" + dateReceived +
                ", unitsReceived=" + unitsReceived +
                ", manufacture='" + manufacture + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                '}';
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
