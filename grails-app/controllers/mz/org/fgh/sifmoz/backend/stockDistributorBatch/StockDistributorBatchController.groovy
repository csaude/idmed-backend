package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.drugDistributor.DrugDistributor
import mz.org.fgh.sifmoz.backend.drugDistributor.DrugDistributorService
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustmentService
import mz.org.fgh.sifmoz.backend.stockoperation.StockOperationType
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMoviment
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMovimentService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.*

class StockDistributorBatchController extends RestfulController {

    IStockDistributorBatchService stockDistributorBatchService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockDistributorBatchController() {
        super(Stock)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockDistributorBatchService.list(params)
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(stockDistributorBatchService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockDistributorBatch stockDistributorBatch = new StockDistributorBatch()
        def objectJSON = request.JSON
        stockDistributorBatch = objectJSON as StockDistributorBatch

        stockDistributorBatch.beforeInsert()
        stockDistributorBatch.validate()

        if (objectJSON.id) {
            stockDistributorBatch.id = UUID.fromString(objectJSON.id)
        }
        if (stockDistributorBatch.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributorBatch.errors
            return
        }

        try {
           save(stockDistributorBatch)


        } catch (ValidationException e) {
            respond stockDistributorBatch.errors
            return
        }

        respond stockDistributorBatch, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON

        StockDistributorBatch stockDistributorBatch = StockDistributorBatch.findWhere(id: objectJSON.id)

        if (stockDistributorBatch == null) {
            render status: NOT_FOUND
            return
        }
        if (stockDistributorBatch.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributorBatch.errors
            return
        }

        try {


            stockDistributorBatch.quantity = Integer.parseInt(objectJSON.getAt("quantity").toString())
            stockDistributorBatch.stock = Drug.findWhere(id: objectJSON?.getAt("stock")?.getAt("id")?.toString())
            stockDistributorBatch.drugDistributor = Clinic.findWhere(id: objectJSON?.getAt("drugDistributor")?.getAt("id")?.toString())

            stockDistributorBatchService.save(stockDistributorBatch)
        } catch (ValidationException e) {
            respond stockDistributorBatch.errors
            return
        }

        respond stockDistributorBatch, [status: OK, view: "show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockDistributorBatchService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }


    def getStockDistributorBatchByStockDistributorId(String stockDistributorId) {
        respond stockDistributorBatchService.getStockDistributorBatchByStockDistributorId(stockDistributorId)
    }

}
