package mz.org.fgh.sifmoz.backend.stockDistributor

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stock.StockService
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatch
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatchService
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntranceService
import mz.org.fgh.sifmoz.backend.stockoperation.StockOperationType
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMoviment
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMovimentService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.*

class StockDistributorController extends RestfulController {

    IStockDistributorService stockDistributorService
    StockDistributorBatchService stockDistributorBatchService
    StockService stockService
    ReferedStockMovimentService referedStockMovimentService
    StockEntranceService stockEntranceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockDistributorController() {
        super(StockDistributor)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockDistributorService.list(params)
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockDistributorService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockDistributor stockDistributor = new StockDistributor()
        def objectJSON = request.JSON
        stockDistributor = objectJSON as StockDistributor

        stockDistributor.beforeInsert()
        stockDistributor.drugDistributors.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.drugDistributors[index].id)
        }
        stockDistributor.validate()

        if (objectJSON.id) {
            stockDistributor.id = UUID.fromString(objectJSON.id)
        }
        if (stockDistributor.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributor.errors
            return
        }
        try {
            stockDistributorService.save(stockDistributor)
        } catch (ValidationException e) {
            respond stockDistributor.errors
            return
        }

        respond stockDistributor, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        StockDistributor stockDistributoreDb = StockDistributor.get(objectJSON.id)
        if (stockDistributoreDb == null) {
            render status: NOT_FOUND
            return
        }
        stockDistributoreDb.properties = objectJSON
        stockDistributoreDb.drugDistributors.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.drugDistributors[index].id)
        }
        if (stockDistributoreDb.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributoreDb.errors
            return
        }

        try {
            stockDistributorService.save(stockDistributoreDb)
        } catch (ValidationException e) {
            respond stockDistributoreDb.errors
            return
        }

        respond stockDistributoreDb, [status: OK, view: "show"]
    }


    @Transactional
    def delete(String id) {
        if (id == null || stockDistributorService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond stockDistributorService.getAllByClinicId(clinicId, offset, max)
    }
}
