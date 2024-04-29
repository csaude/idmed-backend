package mz.org.fgh.sifmoz.backend.reports.patients

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class ExpectedPatientsReportController extends MultiThreadRestReportController {

    IExpectedPatientReportService expectedPatientReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ExpectedPatientsReportController() {
        super(ExpectedPatientReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond expectedPatientReportService.list(params), model: [expectedPatientReportCount: expectedPatientReportService.count()]
    }

    def show(Long id) {
        respond expectedPatientReportService.get(id)
    }

    @Transactional
    def save(ExpectedPatientReport expectedPatientReport) {
        if (expectedPatientReport == null) {
            render status: NOT_FOUND
            return
        }
        if (expectedPatientReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond expectedPatientReport.errors
            return
        }

        try {
        } catch (ValidationException e) {
            respond expectedPatientReport.errors
            return
        }

        respond expectedPatientReport, [status: CREATED, view: "show"]
    }

    @Transactional
    def update(ExpectedPatientReport expectedPatientReport) {
        if (expectedPatientReport == null) {
            render status: NOT_FOUND
            return
        }
        if (expectedPatientReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond expectedPatientReport.errors
            return
        }

        try {

        } catch (ValidationException e) {
            respond expectedPatientReport.errors
            return
        }

        respond expectedPatientReport, [status: OK, view: "show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || expectedPatientReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def initReportProcess(ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render getProcessStatus() as JSON
        doProcessReport()
    }

    /**
     * Implementa toda a logica de processamento da informação do relatório
     */
    @Override
    void run() {
        expectedPatientReportService.doSave(expectedPatientReportService.processamentoDados(getSearchParams(), this.processStatus),)
    }

    def getProcessedData(String reportId) {
        List<ExpectedPatientReport> reportObjects = expectedPatientReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
    }

    def printReport(String reportId, String fileType) {
        List<ExpectedPatientReport> reportObjects = expectedPatientReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
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

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }
}
