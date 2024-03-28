package mz.org.fgh.sifmoz.backend.reports.stock


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IUsedStockReportService {

    UsedStockReportTemp get(Serializable id)

    List<UsedStockReportTemp> list(Map args)

    Long count()

    UsedStockReportTemp delete(Serializable id)

    UsedStockReportTemp save(UsedStockReportTemp usedStockReport)

    void processReportUsedStockRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<UsedStockReportTemp> getReportDataByReportId(String reportId)
}
