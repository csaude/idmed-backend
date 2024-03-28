package mz.org.fgh.sifmoz.backend.reports.referralManagement


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IReferredPatientsReportService {
    ReferredPatientsReport get(Serializable id)

    List<ReferredPatientsReport> list(Map args)

    Long count()

    ReferredPatientsReport delete(Serializable id)

    ReferredPatientsReport save(ReferredPatientsReport referredPatientsReport)

    void processReferredAndBackReferredReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor, String refIdaOuVoltaParam)

    void processReportReferredDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    void processReportAbsentReferredDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)


}
