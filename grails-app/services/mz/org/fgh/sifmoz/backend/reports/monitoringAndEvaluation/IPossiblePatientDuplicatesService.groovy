package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor


interface IPossiblePatientDuplicatesService {
    PossiblePatientDuplicatesReport get(Serializable id)

    List<PossiblePatientDuplicatesReport> list(Map args)

    Long count()

    PossiblePatientDuplicatesReport delete(Serializable id)

    PossiblePatientDuplicatesReport save(PossiblePatientDuplicatesReport possiblePatientDuplicatesReport)

    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<PossiblePatientDuplicatesReport> getReportDataByReportId(String reportId)

}
