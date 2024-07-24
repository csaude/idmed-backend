package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class ArvDailyRegisterReportTemp extends BaseEntity{
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

    //ARVDailyRegister Report
    String orderNumber
    String nid
    String patientName
    String patientType
    String ageGroup_0_4
    String ageGroup_5_9
    String ageGroup_10_14
    String ageGroup_Greater_than_15
    String arvType
    String dispensationType
    String therapeuticLine
    Date pickupDate
    Date nextPickupDate
    String regime
    String packId
    String startReason
    String prep
    String ppe
    String patientVisitDetailId
    static hasMany = ['drugQuantityTemps': DrugQuantityTemp]

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
        patientType nullable: true
        drugQuantityTemps nullable: true
        arvType nullable: true
        regime nullable: true
        prep nullable: true
        ppe nullable: true
        packId nullable: true
        patientVisitDetailId nullable: true

    }
    void setDrugQuantityTemps(List<DrugQuantityTemp> items){
        this.drugQuantityTemps= items;
    }

    @Override
    public String toString() {
        return "ArvDailyRegisterReportTemp{" +
                "drugQuantities=" + drugQuantityTemps +
                ", id='" + id + '\'' +
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
                ", nid='" + nid + '\'' +
                ", patientName='" + patientName + '\'' +
                ", patientType='" + patientType + '\'' +
                ", ageGroup_0_4='" + ageGroup_0_4 + '\'' +
                ", ageGroup_5_9='" + ageGroup_5_9 + '\'' +
                ", ageGroup_10_14='" + ageGroup_10_14 + '\'' +
                ", ageGroup_Greater_than_15='" + ageGroup_Greater_than_15 + '\'' +
                ", arvType='" + arvType + '\'' +
                ", dispensationType='" + dispensationType + '\'' +
                ", therapeuticLine='" + therapeuticLine + '\'' +
                ", pickupDate=" + pickupDate +
                ", nextPickupDate=" + nextPickupDate +
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
