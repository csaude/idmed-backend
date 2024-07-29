package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IRegisteredInIdmedReport {

    RegisteredInIdmedReport get(Serializable id)

    List<RegisteredInIdmedReport> list(Map args)

    RegisteredInIdmedReport delete(Serializable id)

    RegisteredInIdmedReport save(RegisteredInIdmedReport registeredInIdmedReport)

    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<RegisteredInIdmedReport> getReportDataByReportId(String reportId)



}