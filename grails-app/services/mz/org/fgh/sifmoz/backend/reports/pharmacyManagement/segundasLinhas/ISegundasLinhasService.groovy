package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.segundasLinhas


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface ISegundasLinhasService {

    SegundasLinhasReport get(Serializable id)

    List<SegundasLinhasReport> list(Map args)

    Long count()

    SegundasLinhasReport delete(Serializable id)

    SegundasLinhasReport save(SegundasLinhasReport segundasLinhasReport)

    void processReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<SegundasLinhasReport> getSegundasLinhasReportById(String reportId)
}
