package mz.org.fgh.sifmoz.backend.stocklevel

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

class StockLevelController extends RestfulController{

    StockLevelService stockLevelService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockLevelController() {
        super(StockLevel)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(stockLevelService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockLevelService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockLevel stockLevel = new StockLevel()
        def objectJSON = request.JSON
        stockLevel = objectJSON as StockLevel

        stockLevel.beforeInsert()
        stockLevel.validate()

        if(objectJSON.id){
            stockLevel.id = UUID.fromString(objectJSON.id)
        }

        if (stockLevel.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockLevel.errors
            return
        }

        try {
            stockLevelService.save(stockLevel)
        } catch (ValidationException e) {
            respond stockLevel.errors
            return
        }

        respond stockLevel, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StockLevel stockLevel) {
        if (stockLevel == null) {
            render status: NOT_FOUND
            return
        }
        if (stockLevel.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockLevel.errors
            return
        }

        try {
            stockLevelService.save(stockLevel)
        } catch (ValidationException e) {
            respond stockLevel.errors
            return
        }

        respond stockLevel, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || stockLevelService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
