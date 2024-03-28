package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation.ArvDailyRegisterReportTemp
import mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation.NotSynchronizingPacksOpenMrsReport


interface INotSynchronizingPacksOpenMrsReportService {

    NotSynchronizingPacksOpenMrsReport get(Serializable id)

    List<NotSynchronizingPacksOpenMrsReport> list(Map args)

    Long count()

    NotSynchronizingPacksOpenMrsReport delete(Serializable id)

    NotSynchronizingPacksOpenMrsReport save(NotSynchronizingPacksOpenMrsReport notSynchronizingPacksOpenMrsReport)

    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<NotSynchronizingPacksOpenMrsReport> getReportDataByReportId(String reportId)


}
