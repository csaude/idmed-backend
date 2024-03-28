package mz.org.fgh.sifmoz.stock;

import mz.org.fgh.sifmoz.backend.drug.Drug;

import java.util.List;

public class DrugFile {
    private String drugId;
    private Drug drug;
    private List<DrugStockFileEvent> drugFileSummary;
    private List<DrugStockFileEvent> drugFileSummaryBatch;


    public Drug getDrug() {
        return drug;
    }

    public String getDrugId() {
        return drugId;
    }

    public void setDrugId(String drugId) {
        this.drugId = drugId;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public List<DrugStockFileEvent> getDrugFileSummary() {
        return drugFileSummary;
    }

    public void setDrugFileSummary(List<DrugStockFileEvent> drugFileSummary) {
        this.drugFileSummary = drugFileSummary;
    }

    public List<DrugStockFileEvent> getDrugFileSummaryBatch() {
        return drugFileSummaryBatch;
    }

    public void setDrugFileSummaryBatch(List<DrugStockFileEvent> drugFileSummaryBatch) {
        this.drugFileSummaryBatch = drugFileSummaryBatch;
    }
}
