package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IErrorLogPatientUpdateOpenMrsReportService {

    NotSynchronizingPacksOpenMrsReport get(Serializable id)

    List<ErrorLogPatientUpdateOpenMrsReport> list(Map args)

    Long count()

    ErrorLogPatientUpdateOpenMrsReport delete(Serializable id)

    ErrorLogPatientUpdateOpenMrsReport save(ErrorLogPatientUpdateOpenMrsReport errorLogPatientUpdateOpenMrsReport)

    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<ErrorLogPatientUpdateOpenMrsReport> getReportDataByReportId(String reportId)


}
