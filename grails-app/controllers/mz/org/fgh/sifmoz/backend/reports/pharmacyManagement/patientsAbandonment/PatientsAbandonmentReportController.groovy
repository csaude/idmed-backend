package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbandonment

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbndonment.IPatientsAbandonmentService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class PatientsAbandonmentReportController extends MultiThreadRestReportController{

    IPatientsAbandonmentService patientsAbandonmentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientsAbandonmentReportController() {
        super(PatientsAbandonmentReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond patientsAbandonmentService.list(params), model:[absentPatientsReportCount: patientsAbandonmentService.count()]
    }

    def show(Long id) {
        respond patientsAbandonmentService.get(id)
    }

    @Transactional
    def save(PatientsAbandonmentReport patientsAbandonmentReport) {
        if (patientsAbandonmentReport == null) {
            render status: NOT_FOUND
            return
        }
        if (patientsAbandonmentReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientsAbandonmentReport.errors
            return
        }

        try {
            patientsAbandonmentService.save(patientsAbandonmentReport)
        } catch (ValidationException e) {
            respond patientsAbandonmentReport.errors
            return
        }

        respond patientsAbandonmentReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientsAbandonmentReport patientsAbandonmentReport) {
        if (patientsAbandonmentReport == null) {
            render status: NOT_FOUND
            return
        }
        if (patientsAbandonmentReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientsAbandonmentReport.errors
            return
        }

        try {
            patientsAbandonmentService.save(patientsAbandonmentReport)
        } catch (ValidationException e) {
            respond patientsAbandonmentReport.errors
            return
        }

        respond patientsAbandonmentReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientsAbandonmentService.delete(id) == null) {
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
        patientsAbandonmentService.processReportAbandonmetDispenseRecords(searchParams, this.processStatus)
    }
    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    def deleteByReportId(String reportId) {
        List<PatientsAbandonmentReport> patientsAbandonmentReports = PatientsAbandonmentReport.findAllByReportId(reportId)
        PatientsAbandonmentReport.deleteAll(patientsAbandonmentReports)
        render status: NO_CONTENT
    }

    def printReport(String reportId,String fileType) {
        List<PatientsAbandonmentReport> patientsAbandonmentReports = patientsAbandonmentService.getReportDataByReportId(reportId)
        render patientsAbandonmentReports as JSON
    }
}
