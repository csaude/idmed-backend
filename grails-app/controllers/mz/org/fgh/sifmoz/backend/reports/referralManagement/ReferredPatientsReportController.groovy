package mz.org.fgh.sifmoz.backend.reports.referralManagement

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


class ReferredPatientsReportController extends MultiThreadRestReportController{

    IReferredPatientsReportService referredPatientsReportService
    public static final String PROCESS_STATUS_PROCESSING_PACKS = "A processar pacientes"

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ReferredPatientsReportController() {
        super(ReferredPatientsReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond referredPatientsReportService.list(params), model:[referredPatientsReportCount: referredPatientsReportService.count()]
    }

    def show(Long id) {
        respond referredPatientsReportService.get(id)
    }

    @Transactional
    def save(ReferredPatientsReport referredPatientsReport) {
        if (referredPatientsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (referredPatientsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referredPatientsReport.errors
            return
        }

        try {
            referredPatientsReportService.save(referredPatientsReport)
        } catch (ValidationException e) {
            respond referredPatientsReport.errors
            return
        }

        respond referredPatientsReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ReferredPatientsReport referredPatientsReport) {
        if (referredPatientsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (referredPatientsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referredPatientsReport.errors
            return
        }

        try {
            referredPatientsReportService.save(referredPatientsReport)
        } catch (ValidationException e) {
            respond referredPatientsReport.errors
            return
        }

        respond referredPatientsReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || referredPatientsReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def initReportProcess (ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        // render qtyToProcess: qtyRecordsToProcess
        render getProcessStatus() as JSON
        doProcessReport()
    }

    /**
     * Implementa toda a logica de processamento da informação do relatório
     */
    @Override
    void run() {
        if(searchParams.reportType.equals("HISTORICO_LEVANTAMENTO_PACIENTES_REFERIDOS")) {
            referredPatientsReportService.processReportReferredDispenseRecords(searchParams,this.processStatus)
        } else if (searchParams.reportType.equals("REFERIDOS_FALTOSOS_AO_LEVANTAMENTO")) {
            referredPatientsReportService.processReportAbsentReferredDispenseRecords(searchParams,this.processStatus)
        }  else {
            if (searchParams.reportType.equals("REFERIDO_PARA")) {
            referredPatientsReportService.processReferredAndBackReferredReportRecords(searchParams,this.processStatus, "REFERIDO_PARA")
            }
            if (searchParams.reportType.equals("VOLTOU_REFERENCIA")) {
            referredPatientsReportService.processReferredAndBackReferredReportRecords(searchParams,this.processStatus, "VOLTOU_REFERENCIA")
            }
        }
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }


    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    def deleteByReportId(String reportId) {
        List<ReferredPatientsReport> referredPatientsReports = ReferredPatientsReport.findAllByReportId(reportId)
        ReferredPatientsReport.deleteAll(referredPatientsReports)
        render status: NO_CONTENT
    }

    def printReport(String reportId) {

        List<ReferredPatientsReport> referredPatientsReports = ReferredPatientsReport.findAllByReportId(reportId)
        if (referredPatientsReports.size() > 0) {
            render JSONSerializer.setObjectListJsonResponse(referredPatientsReports) as JSON
        } else {
            render status: NO_CONTENT
        }
    }
}
