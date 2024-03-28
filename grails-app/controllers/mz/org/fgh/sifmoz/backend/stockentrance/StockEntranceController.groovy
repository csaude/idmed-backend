package mz.org.fgh.sifmoz.backend.stockentrance

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.stockcenter.StockCenter
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class StockEntranceController extends RestfulController{

    IStockEntranceService stockEntranceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockEntranceController() {
        super(StockCenter)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockEntranceService.list(params)
//        render JSONSerializer.setObjectListJsonResponse(stockEntranceService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockEntranceService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockEntrance stockEntrance = new StockEntrance()
        def objectJSON = request.JSON
        stockEntrance = objectJSON as StockEntrance

        stockEntrance.beforeInsert()
        stockEntrance.stocks.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.stocks[index].id)
        }
        stockEntrance.validate()

        if(objectJSON.id){
            stockEntrance.id = UUID.fromString(objectJSON.id)
        }
        if (stockEntrance.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockEntrance.errors
            return
        }
        try {
            stockEntranceService.save(stockEntrance)
        } catch (ValidationException e) {
            respond stockEntrance.errors
            return
        }

        respond stockEntrance, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        StockEntrance stockEntranceDb = StockEntrance.get(objectJSON.id)
        if (stockEntranceDb == null) {
            render status: NOT_FOUND
            return
        }
        stockEntranceDb.properties = objectJSON
        stockEntranceDb.stocks.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.stocks[index].id)
        }
        if (stockEntranceDb.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockEntranceDb.errors
            return
        }

        try {
            stockEntranceService.save(stockEntranceDb)
        } catch (ValidationException e) {
            respond stockEntranceDb.errors
            return
        }

        respond stockEntranceDb, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockEntranceService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond stockEntranceService.getAllByClinicId(clinicId, offset, max)
    }
}
