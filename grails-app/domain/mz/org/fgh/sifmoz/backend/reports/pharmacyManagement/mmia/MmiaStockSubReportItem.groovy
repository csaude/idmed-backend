package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

class MmiaStockSubReportItem {

    String id
    String reportId
    String fnmCode
    String drugName
    String unit
    int balance
    int initialEntrance
    int outcomes
    int lossesAdjustments
    int inventory
    Date expireDate

    static belongsTo = [mmiaReport: MmiaReport]

    MmiaStockSubReportItem(String reportId, String fnmCode, String drugName, String unit, int initialEntrance, int outcomes, int lossesAdjustments, int inventory) {
        this.reportId = reportId
        this.fnmCode = fnmCode
        this.drugName = drugName
        this.unit = unit
        this.initialEntrance = initialEntrance
        this.outcomes = outcomes
        this.lossesAdjustments = lossesAdjustments
        this.inventory = inventory
    }
    static mapping = {
        id generator: "uuid"
        datasource 'ALL'
    }

    static constraints = {
        mmiaReport nullable: true

    }
}
