package mz.org.fgh.sifmoz.backend.reports.stock

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

class InventoryReportController  extends MultiThreadRestReportController {

    IInventoryReportService inventoryReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    InventoryReportController() {
        super(InventoryReportTemp)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond inventoryReportService.list(params), model:[inventoryReportServiceCount: inventoryReportService.count()]
    }

    def show(Long id) {
        respond inventoryReportService.get(id)
    }

    @Transactional
    def save(InventoryReportTemp inventoryReportTemp) {
        if (inventoryReportTemp == null) {
            render status: NOT_FOUND
            return
        }
        if (inventoryReportTemp.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inventoryReportTemp.errors
            return
        }

        try {
        } catch (ValidationException e) {
            respond inventoryReportTemp.errors
            return
        }

        respond inventoryReportTemp, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(InventoryReportTemp inventoryReportTemp) {
        if (inventoryReportTemp == null) {
            render status: NOT_FOUND
            return
        }
        if (inventoryReportTemp.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inventoryReportTemp.errors
            return
        }

        try {

        } catch (ValidationException e) {
            respond inventoryReportTemp.errors
            return
        }

        respond inventoryReportTemp, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || inventoryReportService.delete(id) == null) {
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
        inventoryReportService.doSave(inventoryReportService.processamentoDados(getSearchParams(),  this.processStatus),)
    }

    def getProcessedData(String reportId) {
        List<InventoryReportTemp> reportObjects = inventoryReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
    }

    def printReport(String reportId, String fileType) {
        List<InventoryReportTemp> reportObjects = inventoryReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
    }

    def getInventoryList(String reportId) {
        List<Inventory> records =  inventoryReportService.getInventoriesList(reportId)
        render records  as JSON
    }


    def printReportByInventoryId(String idInventory, String reportId) {
     def reportObjects = inventoryReportService.getReportDataByInventoryId(idInventory,reportId)
        render  JSONSerializer.setObjectListJsonResponse( reportObjects ) as JSON
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
