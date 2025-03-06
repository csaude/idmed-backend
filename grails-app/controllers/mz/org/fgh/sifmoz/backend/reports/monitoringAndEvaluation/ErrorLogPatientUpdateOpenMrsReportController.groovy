package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class ErrorLogPatientUpdateOpenMrsReportController extends MultiThreadRestReportController{

    IErrorLogPatientUpdateOpenMrsReportService errorLogPatientUpdateOpenMrsReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ErrorLogPatientUpdateOpenMrsReportController() {
        super(ErrorLogPatientUpdateOpenMrsReport)
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond errorLogPatientUpdateOpenMrsReportService.list(params), model:[errorLogPatientUpdateOpenMrsReportCount: errorLogPatientUpdateOpenMrsReportService.count()]
    }

    def show(Long id) {
        respond errorLogPatientUpdateOpenMrsReportService.get(id)
    }

    @Transactional
    def save(ErrorLogPatientUpdateOpenMrsReport errorLogPatientUpdateOpenMrsReport) {
        if (errorLogPatientUpdateOpenMrsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (errorLogPatientUpdateOpenMrsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond errorLogPatientUpdateOpenMrsReport.errors
            return
        }

        try {
            errorLogPatientUpdateOpenMrsReportService.save(errorLogPatientUpdateOpenMrsReport)
        } catch (ValidationException e) {
            respond errorLogPatientUpdateOpenMrsReport.errors
            return
        }

        respond errorLogPatientUpdateOpenMrsReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ErrorLogPatientUpdateOpenMrsReport errorLogPatientUpdateOpenMrsReport) {
        if (errorLogPatientUpdateOpenMrsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (errorLogPatientUpdateOpenMrsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond errorLogPatientUpdateOpenMrsReport.errors
            return
        }

        try {
            errorLogPatientUpdateOpenMrsReportService.save(errorLogPatientUpdateOpenMrsReport)
        } catch (ValidationException e) {
            respond errorLogPatientUpdateOpenMrsReport.errors
            return
        }

        respond errorLogPatientUpdateOpenMrsReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || errorLogPatientUpdateOpenMrsReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    @Override
    void run() {
        errorLogPatientUpdateOpenMrsReportService.processReportRecords(searchParams, this.processStatus)
    }

    def initReportProcess (ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render getProcessStatus() as JSON
        doProcessReport()
    }

    protected int countProcessedRecs() {
        return 0
    }

    protected int countRecordsToProcess() {
        return 0
    }

    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    def printReport(String reportId) {

        List<ErrorLogPatientUpdateOpenMrsReport> errorLogPatientUpdateOpenMrsReportList = ErrorLogPatientUpdateOpenMrsReport.findAllByReportId(reportId)
        if (errorLogPatientUpdateOpenMrsReportList.size() > 0) {
            render JSONSerializer.setObjectListJsonResponse(errorLogPatientUpdateOpenMrsReportList) as JSON
        } else {
            render status: NO_CONTENT
        }
    }

    def deleteByReportId(String reportId) {
        List<ErrorLogPatientUpdateOpenMrsReport> errorLogPatientUpdateOpenMrsReportList = ErrorLogPatientUpdateOpenMrsReport.findAllByReportId(reportId)
        ErrorLogPatientUpdateOpenMrsReport.deleteAll(errorLogPatientUpdateOpenMrsReportList)
        render status: NO_CONTENT
    }
}
