package mz.org.fgh.sifmoz.backend.stockcenter

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class StockCenterController extends RestfulController{

    StockCenterService stockCenterService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockCenterController() {
        super(StockCenter)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(stockCenterService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockCenterService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockCenter stockCenter = new StockCenter()
        def objectJSON = request.JSON
        stockCenter = objectJSON as StockCenter

        stockCenter.beforeInsert()
        stockCenter.validate()

        if(objectJSON.id){
            stockCenter.id = UUID.fromString(objectJSON.id)
        }
        if (stockCenter.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockCenter.errors
            return
        }

        try {
            stockCenterService.save(stockCenter)
        } catch (ValidationException e) {
            respond stockCenter.errors
            return
        }

        respond stockCenter, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StockCenter stockCenter) {
        if (stockCenter == null) {
            render status: NOT_FOUND
            return
        }
        if (stockCenter.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockCenter.errors
            return
        }

        try {
            stockCenterService.save(stockCenter)
        } catch (ValidationException e) {
            respond stockCenter.errors
            return
        }

        respond stockCenter, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || stockCenterService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
