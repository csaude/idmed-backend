package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IBalanceteService {
    BalanceteReport get(Serializable id)

    List<BalanceteReport> list(Map args)

    Long count()

    BalanceteReport delete(Serializable id)

    BalanceteReport save(BalanceteReport balanceteReport)

    void processReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<BalanceteReport> getBalanceteReportById(String reportId)
}
