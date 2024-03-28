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

class StockDestructionAdjustmentController extends RestfulController{

    StockDestructionAdjustmentService stockDestructionAdjustmentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockDestructionAdjustmentController() {
        super(StockDestructionAdjustment)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(stockDestructionAdjustmentService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(stockDestructionAdjustmentService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockDestructionAdjustment stockDestructionAdjustment = new StockDestructionAdjustment()
        def objectJSON = request.JSON
        stockDestructionAdjustment = objectJSON as StockDestructionAdjustment

        stockDestructionAdjustment.beforeInsert()
        stockDestructionAdjustment.validate()

        if(objectJSON.id){
            stockDestructionAdjustment.id = UUID.fromString(objectJSON.id)
        }
        if (stockDestructionAdjustment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDestructionAdjustment.errors
            return
        }

        try {
            stockDestructionAdjustmentService.save(stockDestructionAdjustment)
        } catch (ValidationException e) {
            respond stockDestructionAdjustment.errors
            return
        }

        respond stockDestructionAdjustment, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StockDestructionAdjustment stockDestructionAdjustment) {
        if (stockDestructionAdjustment == null) {
            render status: NOT_FOUND
            return
        }
        if (stockDestructionAdjustment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDestructionAdjustment.errors
            return
        }

        try {
            stockDestructionAdjustmentService.save(stockDestructionAdjustment)
        } catch (ValidationException e) {
            respond stockDestructionAdjustment.errors
            return
        }

        respond stockDestructionAdjustment, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockDestructionAdjustmentService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
