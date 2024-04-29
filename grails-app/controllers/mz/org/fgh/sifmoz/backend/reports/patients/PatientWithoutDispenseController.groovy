package mz.org.fgh.sifmoz.backend.reports.patients

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

class PatientWithoutDispenseController extends MultiThreadRestReportController {

    IPatientWithoutDispenseReportService patientWithoutDispenseReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientWithoutDispenseController() {
        super(PatientWithoutDispenseReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond patientWithoutDispenseReportService.list(params), model:[patientWithoutDispenseReportCount: patientWithoutDispenseReportService.count()]
    }

    def show(Long id) {
        respond patientWithoutDispenseReportService.get(id)
    }

    @Transactional
    def save(PatientWithoutDispenseReport patientWithoutDispenseReport) {
        if (patientWithoutDispenseReport == null) {
            render status: NOT_FOUND
            return
        }
        if (patientWithoutDispenseReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientWithoutDispenseReport.errors
            return
        }

        try {
        } catch (ValidationException e) {
            respond patientWithoutDispenseReport.errors
            return
        }

        respond patientWithoutDispenseReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientWithoutDispenseReport patientWithoutDispenseReport) {
        if (patientWithoutDispenseReport == null) {
            render status: NOT_FOUND
            return
        }
        if (patientWithoutDispenseReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientWithoutDispenseReport.errors
            return
        }

        try {

        } catch (ValidationException e) {
            respond patientWithoutDispenseReport.errors
            return
        }

        respond patientWithoutDispenseReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientWithoutDispenseReportService.delete(id) == null) {
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

    /**
     * Implementa toda a logica de processamento da informação do relatório
     */
    @Override
    void run() {
        patientWithoutDispenseReportService.doSave(patientWithoutDispenseReportService.processamentoDados(getSearchParams(),  this.processStatus),)
    }

    def getProcessedData(String reportId) {
        List<PatientWithoutDispenseReport> reportObjects = patientWithoutDispenseReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
    }

    def printReport(String reportId, String fileType) {
        List<PatientWithoutDispenseReport> reportObjects = patientWithoutDispenseReportService.getReportDataByReportId(reportId)
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
