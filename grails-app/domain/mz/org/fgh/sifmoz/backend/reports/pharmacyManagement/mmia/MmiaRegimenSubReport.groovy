package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail

class MmiaRegimenSubReport {
    String id
    String reportId
    String code
    String regimen
    String line
    String lineCode
    String line1
    int totalline1
    int totaldcline1
    String line2
    int totalline2
    int totaldcline2
    String line3
    int totalline3
    int totaldcline3
    String line4
    int totalline4
    int totaldcline4
    int totalPatients
    int cumunitaryClinic

        static belongsTo = [mmiaReport: MmiaReport]

    MmiaRegimenSubReport (PrescriptionDetail detail, String reportId, boolean isReferido) {
        this.reportId = reportId
        this.code = detail.getTherapeuticRegimen().getCode()
        this.regimen = detail.getTherapeuticRegimen().getDescription()
        this.lineCode = detail.getTherapeuticLine().getCode()
        this.line = detail.getTherapeuticLine().getDescription()
        isReferido ? addpatientDC() : addpatient()
    }
    static mapping = {
        id generator: "uuid"
    }
    static constraints = {
        mmiaReport nullable: true
        line nullable: true
         lineCode nullable: true
         line1 nullable: true
         totalline1 nullable: true
         totaldcline1 nullable: true
         line2 nullable: true
         totalline2 nullable: true
         totaldcline2 nullable: true
         line3 nullable: true
         totalline3 nullable: true
         totaldcline3 nullable: true
         line4 nullable: true
         totalline4 nullable: true
         totaldcline4 nullable: true
    }

    def addpatient() {
        this.totalPatients ++
    }

    def addpatientDC() {
        this.cumunitaryClinic ++
    }
}
