package mz.org.fgh.sifmoz.backend.reports.patients

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IActivePatientReportService {

    ActivePatientReport get(Serializable id)

    List<ActivePatientReport> list(Map args)

    Long count()

    ActivePatientReport delete(Serializable id)

    List<ActivePatientReport> processamentoDados (ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor)

    ActivePatientReport save(ActivePatientReport activeOrFaltosoPatientReport)

    List<ActivePatientReport> getReportDataByReportId(String reportId)

    void doSave(List<ActivePatientReport> activePatientsReport)
}
