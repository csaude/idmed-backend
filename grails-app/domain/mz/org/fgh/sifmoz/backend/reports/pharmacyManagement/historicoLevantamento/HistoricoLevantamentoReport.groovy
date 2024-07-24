package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.historicoLevantamento

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class HistoricoLevantamentoReport extends BaseEntity {
    String id
    String reportId
    String periodType
    int period
    int year
    Date startDate
    Date endDate
    String province
    String district

    String nid
    String firstNames
    String middleNames
    String lastNames
    String cellphone
    String tipoTarv
    Date pickUpDate
    Date nexPickUpDate
    String startReason
    String therapeuticalRegimen
    String dispenseType
    String age
    String dispenseMode
    String clinicalService
    String clinic
    String patientType
    String clinicsector
    String idmeduser

    public HistoricoLevantamentoReport(){

    }

    HistoricoLevantamentoReport(String nid, String firstNames, String middleNames, String lastNames, String cellphone, String tipoTarv, String startReason, String therapeuticalRegimen, String dispenseType, String dispenseMode, String clinicalService, String clinicsector, String idmeduser) {
        this.nid = nid
        this.firstNames = firstNames
        this.middleNames = middleNames
        this.lastNames = lastNames
        this.cellphone = cellphone
        this.tipoTarv = tipoTarv
        this.startReason = startReason
        this.therapeuticalRegimen = therapeuticalRegimen
        this.dispenseType = dispenseType
        this.dispenseMode = dispenseMode
        this.clinicalService = clinicalService
        this.clinicsector = clinicsector
        this.idmeduser = idmeduser
    }
    static constraints = {
        id generator: "uuid"
        periodType nullable: false , inList: ['MONTH','QUARTER','SEMESTER','ANNUAL', 'SPECIFIC']
        startDate nullable: true
        endDate nullable: true
        startReason nullable: true
        patientType nullable: true
        idmeduser nullable: true
    }

    static mapping = {
        datasource 'ALL'
    }

    @Override
    public String toString() {
        return "HistoricoLevantamentoReport{" +
                " id='" + id + '\'' +
                ", reportId='" + reportId + '\'' +
                ", periodType='" + periodType + '\'' +
                ", period=" + period +
                ", year=" + year +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", nid='" + nid + '\'' +
                ", firstNames='" + firstNames + '\'' +
                ", middleNames='" + middleNames + '\'' +
                ", lastNames='" + lastNames + '\'' +
                ", cellphone='" + cellphone + '\'' +
                ", tipoTarv='" + tipoTarv + '\'' +
                ", pickUpDate=" + pickUpDate +
                ", nexPickUpDate=" + nexPickUpDate +
                ", startReason='" + startReason + '\'' +
                ", therapeuticalRegimen='" + therapeuticalRegimen + '\'' +
                ", dispenseType='" + dispenseType + '\'' +
                ", age='" + age + '\'' +
                ", dispenseMode='" + dispenseMode + '\'' +
                ", clinicalService='" + clinicalService + '\'' +
                ", clinic='" + clinic + '\'' +
                ", clinicsector='" + clinicsector + '\'' +
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
