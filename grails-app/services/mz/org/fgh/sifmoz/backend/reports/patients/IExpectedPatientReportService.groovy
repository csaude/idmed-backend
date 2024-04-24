package mz.org.fgh.sifmoz.backend.reports.patients

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IExpectedPatientReportService {

    ExpectedPatientReport get(Serializable id)

    List<ExpectedPatientReport> list(Map args)

    Long count()

    ExpectedPatientReport delete(Serializable id)

    List<ExpectedPatientReport> processamentoDados (ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor)

    ExpectedPatientReport save(ExpectedPatientReport expectedPatientReport)

    List<ExpectedPatientReport> getReportDataByReportId(String reportId)

    void doSave(List<ExpectedPatientReport> expectedPatientReport)

}