package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

class PossiblePatientDuplicatesReportController extends MultiThreadRestReportController{

    IPossiblePatientDuplicatesService possiblePatientDuplicatesService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PossiblePatientDuplicatesReportController() {
        super(PossiblePatientDuplicatesReportController)
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond possiblePatientDuplicatesService.list(params), model:[possiblePatientDuplicatesReportCount: possiblePatientDuplicatesService.count()]
    }

    def show(Long id) {
        respond possiblePatientDuplicatesService.get(id)
    }
    @Transactional
    def save(PossiblePatientDuplicatesReport possiblePatientDuplicatesReport) {
        if (possiblePatientDuplicatesReport == null) {
            render status: NOT_FOUND
            return
        }
        if (possiblePatientDuplicatesReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond possiblePatientDuplicatesReport.errors
            return
        }

        try {
            possiblePatientDuplicatesService.save(possiblePatientDuplicatesReport)
        } catch (ValidationException e) {
            respond possiblePatientDuplicatesReport.errors
            return
        }

        respond possiblePatientDuplicatesReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PossiblePatientDuplicatesReport possiblePatientDuplicatesReport) {
        if (possiblePatientDuplicatesReport == null) {
            render status: NOT_FOUND
            return
        }
        if (possiblePatientDuplicatesReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond possiblePatientDuplicatesReport.errors
            return
        }

        try {
            possiblePatientDuplicatesService.save(possiblePatientDuplicatesReport)
        } catch (ValidationException e) {
            respond possiblePatientDuplicatesReport.errors
            return
        }

        respond possiblePatientDuplicatesReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || possiblePatientDuplicatesService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    @Override
    void run() {
        possiblePatientDuplicatesService.processReportRecords(searchParams, this.processStatus)
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

        List<PossiblePatientDuplicatesReport> possiblePatientDuplicatesReportList = PossiblePatientDuplicatesReport.findAllByReportId(reportId)
        if (possiblePatientDuplicatesReportList.size() > 0) {
            render JSONSerializer.setObjectListJsonResponse(possiblePatientDuplicatesReportList) as JSON
        } else {
            render status: NO_CONTENT
        }
    }

    def deleteByReportId(String reportId) {
        List<PossiblePatientDuplicatesReport> possiblePatientDuplicatesReportList = PossiblePatientDuplicatesReport.findAllByReportId(reportId)
        PossiblePatientDuplicatesReport.deleteAll(possiblePatientDuplicatesReportList)
        render status: NO_CONTENT
    }
}
