package mz.org.fgh.sifmoz.backend.stock

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.IStockDistributorBatchService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class StockController extends RestfulController{

    IStockService stockService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockController() {
        super(Stock)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockService.list(params)
//        render JSONSerializer.setObjectListJsonResponse(stockService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(stockService.get(id)) as JSON
    }

    @Transactional
    def save() {
        Stock stock = new Stock()
        def objectJSON = request.JSON
        stock = objectJSON as Stock

        stock.beforeInsert()
        stock.validate()

        if(objectJSON.id){
            stock.id = UUID.fromString(objectJSON.id)
        }
        if (stock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stock.errors
            return
        }

        try {
            stockService.save(stock)
        } catch (ValidationException e) {
            respond stock.errors
            return
        }

        respond stock, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON

        Stock stock = Stock.findWhere(id: objectJSON.id)

        if (stock == null) {
            render status: NOT_FOUND
            return
        }
        if (stock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stock.errors
            return
        }

        try {

            stock.expireDate = Utilities.dateformatToYYYYMMDD(objectJSON.getAt("expireDate").toString())
            stock.shelfNumber = objectJSON.getAt("shelfNumber").toString()
            stock.unitsReceived = Integer.parseInt(objectJSON.getAt("unitsReceived").toString())
            stock.stockMoviment = Integer.parseInt(objectJSON.getAt("stockMoviment").toString())
            stock.manufacture = objectJSON.getAt("manufacture").toString()
            stock.batchNumber = objectJSON.getAt("batchNumber").toString()
            stock.drug = Drug.findWhere(id: objectJSON?.getAt("drug")?.getAt("id")?.toString())

            stockService.save(stock)
        } catch (ValidationException e) {
            respond stock.errors
            return
        }

        respond stock, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
//    @Transactional
    def checkStockStatus(String idPrescribedDrug, Date prescriptionDate, int qtyPrescribed) {
        def isValid = stockService.validateStock(idPrescribedDrug, prescriptionDate, qtyPrescribed)
        render isValid
    }

//    @Transactional
    def getValidStockByDrugAndPickUpDate(String idPackagedDrug, Date packageDate) {
        def stocks = stockService.getValidStockByDrugAndPickUpDate(idPackagedDrug, packageDate)
        render JSONSerializer.setObjectListJsonResponse(stocks) as JSON
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond stockService.getAllByClinicId(clinicId, offset, max)
    }
}
