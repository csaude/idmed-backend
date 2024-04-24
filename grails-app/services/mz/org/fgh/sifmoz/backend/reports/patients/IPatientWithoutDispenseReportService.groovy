package mz.org.fgh.sifmoz.backend.reports.patients

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IPatientWithoutDispenseReportService {

    PatientWithoutDispenseReport get(Serializable id)

    List<PatientWithoutDispenseReport> list(Map args)

    Long count()

    PatientWithoutDispenseReport delete(Serializable id)

    List<PatientWithoutDispenseReport> processamentoDados (ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor)

    PatientWithoutDispenseReport save(PatientWithoutDispenseReport patientWithoutDispenseReport)

    List<PatientWithoutDispenseReport> getReportDataByReportId(String reportId)

    void doSave(List<PatientWithoutDispenseReport> patientWithoutDispenseReport)

}