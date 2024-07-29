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

class RegisteredInIdmedReportController extends MultiThreadRestReportController{

    RegisteredInIdmedReportController() {
        super(RegisteredInIdmedReport)
    }

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    IRegisteredInIdmedReport registeredInIdmedReportService


    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond registeredInIdmedReportService.list(params), model:[registeredInIdmedCount: RegisteredInIdmedReport.count()]
    }

    def show(Long id) {
        respond registeredInIdmedReportService.get(id)
    }

    @Transactional
    def save(RegisteredInIdmedReport registeredInIdmed) {
        if (registeredInIdmed == null) {
            render status: NOT_FOUND
            return
        }
        if (registeredInIdmed.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond registeredInIdmed.errors
            return
        }

        try {
            registeredInIdmedReportService.save(registeredInIdmed)
        } catch (ValidationException e) {
            respond registeredInIdmed.errors
            return
        }

        respond registeredInIdmed, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(RegisteredInIdmedReport registeredInIdmed) {
        if (registeredInIdmed == null) {
            render status: NOT_FOUND
            return
        }
        if (registeredInIdmed.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond registeredInIdmed.errors
            return
        }

        try {
            registeredInIdmedReportService.save(registeredInIdmed)
        } catch (ValidationException e) {
            respond registeredInIdmed.errors
            return
        }

        respond registeredInIdmed, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || registeredInIdmedReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    @Override
    void run() {
        registeredInIdmedReportService.processReportRecords(searchParams, this.processStatus)
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

        List<RegisteredInIdmedReport> registeredInIdmedReportList = RegisteredInIdmedReport.findAllByReportId(reportId)
        if (registeredInIdmedReportList.size() > 0) {
            render JSONSerializer.setObjectListJsonResponse(registeredInIdmedReportList) as JSON
        } else {
            render status: NO_CONTENT
        }
    }

    def deleteByReportId(String reportId) {
        List<RegisteredInIdmedReport> registeredInIdmedReportList = RegisteredInIdmedReport.findAllByReportId(reportId)
        RegisteredInIdmedReport.deleteAll(registeredInIdmedReportList)
        render status: NO_CONTENT
    }
}
