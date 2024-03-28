package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IArvDailyRegisterReportService {

    ArvDailyRegisterReportTemp get(Serializable id)

    List<ArvDailyRegisterReportTemp> list(Map args)

    Long count()

    ArvDailyRegisterReportTemp delete(Serializable id)

    ArvDailyRegisterReportTemp save(ArvDailyRegisterReportTemp reportTemp)

    DrugQuantityTemp save(DrugQuantityTemp reportTemp)

    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<ArvDailyRegisterReportTemp> getReportDataByReportId(String reportId)

    List<DrugQuantityTemp> getSubReportDataById(String id)


}
