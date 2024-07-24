package mz.org.fgh.sifmoz.backend.reports.patients

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class ExpectedPatientReport extends BaseEntity{
    String id
    String reportId
    String periodType
    int year
    Date startDate
    Date endDate

    String firstNames
    String middleNames
    String lastNames
    String province
    String district
    String clinic // Report Parameter
    String nid
    Date nextPickUpDate
    String therapeuticRegimen
    String dispenseType
    String clinicSectorName

    ExpectedPatientReport() {
    }

    ExpectedPatientReport(String reportId, String firstNames, String middleNames, String lastNames, String therapeuticRegimen,  String periodType, String dispenseType, int year, Date startDate, Date endDate, String clinicSectorName) {
        this.reportId = reportId
        this.firstNames = firstNames
        this.middleNames = middleNames
        this.lastNames = lastNames
        this.therapeuticRegimen = therapeuticRegimen
        this.periodType = periodType
        this.year = year
        this.startDate = startDate
        this.endDate = endDate
        this.dispenseType = dispenseType
        this.clinicSectorName = clinicSectorName
    }
    static constraints = {
        id generator: "uuid"
        clinic nullable: true
        province nullable: true
        district nullable: true
        periodType nullable: false, inList: ['MONTH', 'QUARTER', 'SEMESTER', 'ANNUAL', 'SPECIFIC']
        startDate nullable: true
        endDate nullable: true
        year nullable: true
        dispenseType nullable: true
        clinicSectorName nullable: true
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
                ", provinceId='" + province + '\'' +
                ", districtId='" + district + '\'' +
                ", clinic='" + clinic + '\'' +
                ", nid='" + nid + '\'' +
                ", nextPickUpDate=" + nextPickUpDate +
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