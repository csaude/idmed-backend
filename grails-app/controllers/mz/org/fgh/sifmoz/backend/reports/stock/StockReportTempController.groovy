package mz.org.fgh.sifmoz.backend.reports.stock

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class StockReportTempController extends MultiThreadRestReportController {


    IStockReportService stockReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockReportTempController() {
        super(StockReportTemp)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockReportService.list(params), model: [stockReportReportCount: stockReportService.count()]
    }

    def show(Long id) {
        respond stockReportService.get(id)
    }

    @Transactional
    def save(StockReportTemp stockReport) {
        if (stockReport == null) {
            render status: NOT_FOUND
            return
        }
        if (stockReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockReport.errors
            return
        }

        try {
            stockReportService.save(stockReport)
        } catch (ValidationException e) {
            respond stockReport.errors
            return
        }

        respond stockReport, [status: CREATED, view: "show"]
    }


    @Transactional
    def update(StockReportTemp stockReport) {
        if (stockReport == null) {
            render status: NOT_FOUND
            return
        }
        if (stockReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockReport.errors
            return
        }

        try {
            IStockReportService.save(stockReport)
        } catch (ValidationException e) {
            respond stockReport.errors
            return
        }

        respond stockReport, [status: OK, view: "show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || IStockReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def initReportProcess(ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render JSONSerializer.setJsonObjectResponse(this.processStatus) as JSON
        doProcessReport()
    }


   def printReport(String reportId, String fileType) {
       List<StockReportTemp> itemsReport = stockReportService.getReportDataByReportId(reportId)  
       if (Utilities.listHasElements(itemsReport)) { 
           render itemsReport as JSON
       } else {
           render status: NO_CONTENT
       }
   }


    @Override
    void run() {
        stockReportService.processReportRecords(searchParams,this.processStatus)

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
