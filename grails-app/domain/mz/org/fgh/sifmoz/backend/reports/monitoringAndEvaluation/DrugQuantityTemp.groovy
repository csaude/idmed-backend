package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import com.fasterxml.jackson.annotation.JsonBackReference

class DrugQuantityTemp {
    String id
    String drugName

    String getNid() {
        return nid
    }

    void setNid(String nid) {
        this.nid = nid
    }
    String nid
    long quantity
    static belongsTo = ['arvDailyRegisterReportTemp': ArvDailyRegisterReportTemp]

    DrugQuantityTemp() {

    }

    DrugQuantityTemp(String drugName, long quantity) {
        this.drugName = drugName
        this.quantity = quantity
    }

    static constraints = {
        id generator: "uuid"
    }

    @Override
    public String toString() {
        return "DrugQuantityTemp{" +
                "id=" + id +
                ", version=" + version +
                ", drugName='" + drugName + '\'' +
                ", quantity=" + quantity +
                '}';
    }
}
