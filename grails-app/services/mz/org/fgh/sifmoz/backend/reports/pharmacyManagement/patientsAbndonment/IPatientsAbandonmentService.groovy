package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbndonment


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbandonment.PatientsAbandonmentReport

interface IPatientsAbandonmentService {

    PatientsAbandonmentReport get(Serializable id)

    List<PatientsAbandonmentReport> list(Map args)

    Long count()

    PatientsAbandonmentReport delete(Serializable id)

    PatientsAbandonmentReport save(PatientsAbandonmentReport patientsAbandonment)

    void processReportAbandonmetDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<PatientsAbandonmentReport> getReportDataByReportId(String reportId)

}
