package mz.org.fgh.sifmoz.backend.reports.common

import grails.gorm.services.Query
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.stock.Stock


interface IReportProcessMonitorService {

    ReportProcessMonitor get(Serializable id)

    List<ReportProcessMonitor> list(Map args)

    Long count()

    ReportProcessMonitor delete(Serializable id)

    ReportProcessMonitor save(ReportProcessMonitor reportProcessStatus)

    ReportProcessMonitor getByReportId(String reportId)
}
