package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.linhasUsadas

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.AbsentPatientsReport
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class LinhasUsadasReportController extends MultiThreadRestReportController{


    ILinhasUsadasService linhasUsadasService
//    LinhasUsadasReport curLinhaUsadaReport

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

//    protected IReportProcessMonitorService reportProcessMonitorService;

    LinhasUsadasReportController() {
        super(LinhasUsadasReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond linhasUsadasService.list(params), model:[linhasUsadasReportCount: linhasUsadasService.count()]
    }

    def show(String id) {
        respond linhasUsadasService.get(id)
    }

    @Transactional
    def save(LinhasUsadasReport linhasUsadasReport) {
        if (linhasUsadasReport == null) {
            render status: NOT_FOUND
            return
        }
        if (linhasUsadasReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond linhasUsadasReport.errors
            return
        }

        try {
            linhasUsadasService.save(linhasUsadasReport)
        } catch (ValidationException e) {
            respond linhasUsadasReport.errors
            return
        }

        respond linhasUsadasReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(LinhasUsadasReport linhasUsadasReport) {
        if (linhasUsadasReport == null) {
            render status: NOT_FOUND
            return
        }
        if (linhasUsadasReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond linhasUsadasReport.errors
            return
        }

        try {
            linhasUsadasService.save(linhasUsadasReport)
        } catch (ValidationException e) {
            respond linhasUsadasReport.errors
            return
        }

        respond linhasUsadasReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || linhasUsadasService.delete(id) == null) {
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
//        initLinhasUsadasRecord()
        linhasUsadasService.processReport(searchParams, this.processStatus)
    }

    def printReport(String reportId, String fileType) {
//        Map<String, Object> params = new HashMap<>()
//        byte[] report = super.printReport(reportId, fileType, getReportsPath()+"pharmacyManagement/LinhasUsadas.jrxml", params)
//        render(file: report, contentType: 'application/'+fileType.equals("PDF")? 'pdf' : 'xls')

        List<LinhasUsadasReport> linhasUsadasReportList = linhasUsadasService.getLinhasUsadasReportById(reportId)
        render linhasUsadasReportList as JSON
    }

    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    private void initLinhasUsadasRecord() {
        this.curLinhaUsadaReport = new LinhasUsadasReport()
        this.curLinhaUsadaReport.setReportId(getSearchParams().getId())
        this.curLinhaUsadaReport.setClinicId(getSearchParams().getClinicId())
        this.curLinhaUsadaReport.setPeriodType(getSearchParams().getPeriodType())
        this.curLinhaUsadaReport.setPeriod(Integer.valueOf(getSearchParams().getPeriod()))
        this.curLinhaUsadaReport.setYear(getSearchParams().getYear())
        this.curLinhaUsadaReport.setStartDate(getSearchParams().getStartDate())
        this.curLinhaUsadaReport.setEndDate(getSearchParams().getEndDate())
    }
}