package mz.org.fgh.sifmoz.backend.reports.stock

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class BalanceteReportController  extends MultiThreadRestReportController {


    IBalanceteService balanceteService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

//    protected IReportProcessMonitorService reportProcessMonitorService;

    BalanceteReportController() {
        super(BalanceteReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond balanceteService.list(params), model:[linhasUsadasReportCount: balanceteService.count()]
    }

    def show(String id) {
        respond balanceteService.get(id)
    }

    @Transactional
    def save(BalanceteReport balanceteReport) {
        if (balanceteReport == null) {
            render status: NOT_FOUND
            return
        }
        if (balanceteReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond balanceteReport.errors
            return
        }

        try {
            balanceteService.save(balanceteReport)
        } catch (ValidationException e) {
            respond balanceteReport.errors
            return
        }

        respond balanceteReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(BalanceteReport balanceteReport) {
        if (balanceteReport == null) {
            render status: NOT_FOUND
            return
        }
        if (balanceteReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond balanceteReport.errors
            return
        }

        try {
            balanceteService.save(balanceteReport)
        } catch (ValidationException e) {
            respond balanceteReport.errors
            return
        }

        respond balanceteReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || balanceteService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }
        render status: NO_CONTENT
    }

    def initReportProcess (ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render getProcessStatus() as JSON
        doProcessReport()
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    @Override
    void run() {
        balanceteService.processReport(searchParams, this.processStatus)
    }

    def printReport(String reportId, String fileType) {

        List<BalanceteReport> balanceteReports = balanceteService.getBalanceteReportById(reportId)
        render balanceteReports as JSON
    }

    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

}