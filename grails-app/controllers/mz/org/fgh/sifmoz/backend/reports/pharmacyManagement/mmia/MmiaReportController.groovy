package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.converters.JSON
import grails.util.BuildSettings
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class MmiaReportController extends MultiThreadRestReportController{

    IMmiaReportService mmiaReportService
    MmiaReport curMmiaReport
    IMmiaStockSubReportService mmiaStockSubReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    MmiaReportController() {
        super(MmiaReport)
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    def printReport(String reportId, String fileType) {
        Map<String, Object> params = new HashMap<>()
        byte[] report = super.printReport(reportId, fileType, getReportsPath()+"pharmacyManagement/MmiaReport.jrxml", params)
        render(file: report, contentType: 'application/'+fileType.equals("PDF")? 'pdf' : 'xls')
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond mmiaReportService.list(params), model:[mmiaReportCount: mmiaReportService.count()]
    }

    def show(String id) {
        MmiaReport mmiaReport = MmiaReport.findByReportId(id)
        if (mmiaReport != null) {
            mmiaReport.setMmiaRegimenSubReportList(MmiaRegimenSubReport.findAllByReportId(id) as Set<MmiaRegimenSubReport>)
            mmiaReport.setMmiaStockSubReportItemList(MmiaStockSubReportItem.findAllByReportId(id) as Set<MmiaStockSubReportItem>)
            mmiaReport.setClinic(Clinic.findById(mmiaReport.clinicId) as Set<Clinic>)
            render JSONSerializer.setJsonObjectResponse(mmiaReport) as JSON
        }  else {
            render status: NO_CONTENT
        }
    }

    @Transactional
    def save(MmiaReport mmiaReport) {
        if (mmiaReport == null) {
            render status: NOT_FOUND
            return
        }
        if (mmiaReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond mmiaReport.errors
            return
        }

        try {
            mmiaReportService.save(mmiaReport)
        } catch (ValidationException e) {
            respond mmiaReport.errors
            return
        }

        respond mmiaReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(MmiaReport mmiaReport) {
        if (mmiaReport == null) {
            render status: NOT_FOUND
            return
        }
        if (mmiaReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond mmiaReport.errors
            return
        }

        try {
            mmiaReportService.save(mmiaReport)
        } catch (ValidationException e) {
            respond mmiaReport.errors
            return
        }

        respond mmiaReport, [status: OK, view:"show"]
    }

    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    @Transactional
    def delete(String id) {
        if (id == null || mmiaReportService.delete(id) == null) {
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
    void run() {
        initMmiaReportRecord()
        searchAndProcessPacks()
        processMmiaStock()
    }

    void processMmiaStock() {
        if(searchParams.reportType.equalsIgnoreCase("MAPA_MENSAL_DE_MEDICAMENTO_DA_TB")) {
            mmiaStockSubReportService.generateMmiaStockSubReportForMMiaTB(getSearchParams(), this.processStatus)
        } else {
            mmiaStockSubReportService.generateMmiaStockSubReport(getSearchParams(), this.processStatus)
        }
    }

    void searchAndProcessPacks() {
        mmiaReportService.processReport(getSearchParams(), this.curMmiaReport, this.processStatus)
    }

    private void initMmiaReportRecord() {
        this.curMmiaReport = new MmiaReport()
        this.curMmiaReport.setReportId(getSearchParams().getId())
        this.curMmiaReport.setClinicId(getSearchParams().getClinicId())
        this.curMmiaReport.setPeriodType(getSearchParams().getPeriodType())
        this.curMmiaReport.setPeriod(Integer.valueOf(getSearchParams().getPeriod()))
        this.curMmiaReport.setYear(getSearchParams().getYear())
        this.curMmiaReport.setStartDate(getSearchParams().getStartDate())
        this.curMmiaReport.setEndDate(getSearchParams().getEndDate())
    }
}
