package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia


import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.service.ClinicalService


interface IMmiaStockSubReportService {

    MmiaStockSubReportItem get(Serializable id)

    List<MmiaStockSubReportItem> list(Map args)

    Long count()

    MmiaStockSubReportItem delete(Serializable id)

    MmiaStockSubReportItem save(MmiaStockSubReportItem mmiaStockSubReport)

    List<MmiaStockSubReportItem> generateMmiaStockSubReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<MmiaStockSubReportItem> generateMmiaStockSubReportForMMiaTB(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)
}
