package mz.org.fgh.sifmoz.backend.reports.common

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(ReportProcessMonitor)
abstract class ReportProcessMonitorService implements IReportProcessMonitorService{

    @Override
    ReportProcessMonitor getByReportId(String reportId) {
        return ReportProcessMonitor.findByReportId(reportId)
    }
}
