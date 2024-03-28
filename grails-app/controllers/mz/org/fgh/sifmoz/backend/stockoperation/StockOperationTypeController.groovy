package mz.org.fgh.sifmoz.backend.stockoperation

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

class StockOperationTypeController extends RestfulController{

    StockOperationTypeService stockOperationTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockOperationTypeController() {
        super(StockOperationType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(stockOperationTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockOperationTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockOperationType stockOperationType = new StockOperationType()
        def objectJSON = request.JSON
        stockOperationType = objectJSON as StockOperationType

        stockOperationType.beforeInsert()
        stockOperationType.validate()

        if(objectJSON.id){
            stockOperationType.id = UUID.fromString(objectJSON.id)
        }
        if (stockOperationType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockOperationType.errors
            return
        }

        try {
            stockOperationTypeService.save(stockOperationType)
        } catch (ValidationException e) {
            respond stockOperationType.errors
            return
        }

        respond stockOperationType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StockOperationType stockOperationType) {
        if (stockOperationType == null) {
            render status: NOT_FOUND
            return
        }
        if (stockOperationType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockOperationType.errors
            return
        }

        try {
            stockOperationTypeService.save(stockOperationType)
        } catch (ValidationException e) {
            respond stockOperationType.errors
            return
        }

        respond stockOperationType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || stockOperationTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
