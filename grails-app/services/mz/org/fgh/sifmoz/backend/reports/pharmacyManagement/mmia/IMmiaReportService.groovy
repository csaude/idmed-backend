package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor


interface IMmiaReportService {

    MmiaReport get(Serializable id)

    List<MmiaReport> list(Map args)

    Long count()

    MmiaReport delete(Serializable id)

    MmiaReport save(MmiaReport mmiaReport)

    void processReport(ReportSearchParams searchParams, MmiaReport curMmiaReport, ReportProcessMonitor processMonitor)
}
