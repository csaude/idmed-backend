package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.converters.JSON
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional


class NotSynchronizingPacksOpenMrsReportController  extends MultiThreadRestReportController{

    INotSynchronizingPacksOpenMrsReportService notSynchronizingPacksOpenMrsReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    NotSynchronizingPacksOpenMrsReportController() {
        super(NotSynchronizingPacksOpenMrsReport)
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond notSynchronizingPacksOpenMrsReportService.list(params), model:[notSynchronizingPacksOpenMrsReportCount: notSynchronizingPacksOpenMrsReportService.count()]
    }

    def show(Long id) {
        respond notSynchronizingPacksOpenMrsReportService.get(id)
    }

    @Transactional
    def save(NotSynchronizingPacksOpenMrsReport notSynchronizingPacksOpenMrsReport) {
        if (notSynchronizingPacksOpenMrsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (notSynchronizingPacksOpenMrsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond notSynchronizingPacksOpenMrsReport.errors
            return
        }

        try {
            notSynchronizingPacksOpenMrsReportService.save(notSynchronizingPacksOpenMrsReport)
        } catch (ValidationException e) {
            respond notSynchronizingPacksOpenMrsReport.errors
            return
        }

        respond notSynchronizingPacksOpenMrsReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(NotSynchronizingPacksOpenMrsReport notSynchronizingPacksOpenMrsReport) {
        if (notSynchronizingPacksOpenMrsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (notSynchronizingPacksOpenMrsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond notSynchronizingPacksOpenMrsReport.errors
            return
        }

        try {
            notSynchronizingPacksOpenMrsReportService.save(notSynchronizingPacksOpenMrsReport)
        } catch (ValidationException e) {
            respond notSynchronizingPacksOpenMrsReport.errors
            return
        }

        respond notSynchronizingPacksOpenMrsReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || notSynchronizingPacksOpenMrsReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    @Override
    void run() {
        notSynchronizingPacksOpenMrsReportService.processReportRecords(searchParams, this.processStatus)
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

        List<NotSynchronizingPacksOpenMrsReport> notSynchronizingPacksOpenMrsReportList = NotSynchronizingPacksOpenMrsReport.findAllByReportId(reportId)
        if (notSynchronizingPacksOpenMrsReportList.size() > 0) {
            render JSONSerializer.setObjectListJsonResponse(notSynchronizingPacksOpenMrsReportList) as JSON
        } else {
            render status: NO_CONTENT
        }
    }

    def deleteByReportId(String reportId) {
        List<NotSynchronizingPacksOpenMrsReport> notSynchronizingPacksOpenMrsReportList = NotSynchronizingPacksOpenMrsReport.findAllByReportId(reportId)
        NotSynchronizingPacksOpenMrsReport.deleteAll(notSynchronizingPacksOpenMrsReportList)
        render status: NO_CONTENT
    }
}
