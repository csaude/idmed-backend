package mz.org.fgh.sifmoz.backend.reports.patients

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class PatientWithoutDispenseReport  extends BaseEntity{

    String id
    String reportId
    String periodType
    int year
    Date startDate
    Date endDate
    String clinic
    String province
    String district


    String firstNames
    String middleNames
    String lastNames
    String nid
    String uuidOpenMrs
    Date createDate


    PatientWithoutDispenseReport() {
    }

    PatientWithoutDispenseReport(String reportId, String firstNames, String middleNames, String lastNames,String periodType, String clinic, int year, Date startDate, Date endDate, String nid, String uuidOpenMrs, Date createDate) {
        this.reportId = reportId
        this.periodType = periodType
        this.year = year
        this.startDate = startDate
        this.endDate = endDate
        this.clinic = clinic
        this.nid = nid
        this.firstNames = firstNames
        this.middleNames = middleNames
        this.lastNames = lastNames
        this.uuidOpenMrs = uuidOpenMrs
        this.createDate = createDate

    }
    static constraints = {
        id generator: "uuid"
        periodType nullable: false, inList: ['MONTH', 'QUARTER', 'SEMESTER', 'ANNUAL', 'SPECIFIC']
        startDate nullable: true
        endDate nullable: true
        year nullable: true
        middleNames nullable: true
        lastNames nullable: true
        uuidOpenMrs nullable: true
        province nullable: true
        district nullable: true
    }

    static mapping = {
        id generator: "uuid"
        datasource 'ALL'
    }


    @Override
    public String toString() {
        return "ActivePatientReport{" +
                " Id='" + id + '\'' +
                ", reportId='" + reportId + '\'' +
                ", firstNames='" + firstNames + '\'' +
                ", middleNames='" + middleNames + '\'' +
                ", lastNames='" + lastNames + '\'' +
                ", gender='" + gender + '\'' +
                ", cellphone='" + cellphone + '\'' +
                ", provinceId='" + province + '\'' +
                ", districtId='" + district + '\'' +
                ", clinic='" + clinic + '\'' +
                ", nid='" + nid + '\'' +
                ", pickupDate=" + pickupDate +
                ", nextPickUpDate=" + nextPickUpDate +
                ", prescriptionDate=" + prescriptionDate +
                ", therapeuticRegimen='" + therapeuticRegimen + '\'' +
                ", dispenseType='" + dispenseType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", periodType='" + periodType + '\'' +
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
