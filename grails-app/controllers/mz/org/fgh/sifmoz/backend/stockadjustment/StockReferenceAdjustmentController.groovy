package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class StockReferenceAdjustmentController extends RestfulController{

    StockReferenceAdjustmentService stockReferenceAdjustmentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockReferenceAdjustmentController() {
        super(StockReferenceAdjustment)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(stockReferenceAdjustmentService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(stockReferenceAdjustmentService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockReferenceAdjustment stockReferenceAdjustment = new StockReferenceAdjustment()
        def objectJSON = request.JSON
        stockReferenceAdjustment = objectJSON as StockReferenceAdjustment

        stockReferenceAdjustment.beforeInsert()
        stockReferenceAdjustment.validate()

        if(objectJSON.id){
            stockReferenceAdjustment.id = UUID.fromString(objectJSON.id)
        }
        if (stockReferenceAdjustment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockReferenceAdjustment.errors
            return
        }

        try {
            stockReferenceAdjustmentService.save(stockReferenceAdjustment)
        } catch (ValidationException e) {
            respond stockReferenceAdjustment.errors
            return
        }

        respond stockReferenceAdjustment, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StockReferenceAdjustment stockReferenceAdjustment) {
        if (stockReferenceAdjustment == null) {
            render status: NOT_FOUND
            return
        }
        if (stockReferenceAdjustment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockReferenceAdjustment.errors
            return
        }

        try {
            stockReferenceAdjustmentService.save(stockReferenceAdjustment)
        } catch (ValidationException e) {
            respond stockReferenceAdjustment.errors
            return
        }

        respond stockReferenceAdjustment, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockReferenceAdjustmentService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
