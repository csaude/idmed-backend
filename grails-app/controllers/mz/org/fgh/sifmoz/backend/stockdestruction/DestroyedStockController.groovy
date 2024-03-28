package mz.org.fgh.sifmoz.backend.stockdestruction

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

class DestroyedStockController extends RestfulController{

    DestroyedStockService destroyedStockService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DestroyedStockController() {
        super(DestroyedStock)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(destroyedStockService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(destroyedStockService.get(id)) as JSON
    }

    @Transactional
    def save() {
        DestroyedStock destroyedStock = new DestroyedStock()
        def objectJSON = request.JSON
        destroyedStock = objectJSON as DestroyedStock

        destroyedStock.beforeInsert()
        destroyedStock.adjustments.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.adjustments[index].id)
        }
        destroyedStock.validate()

        if(objectJSON.id){
            destroyedStock.id = UUID.fromString(objectJSON.id)
        }
        if (destroyedStock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond destroyedStock.errors
            return
        }

        try {
            destroyedStockService.save(destroyedStock)
        } catch (ValidationException e) {
            respond destroyedStock.errors
            return
        }

        respond destroyedStock, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(DestroyedStock destroyedStock) {
        if (destroyedStock == null) {
            render status: NOT_FOUND
            return
        }
        if (destroyedStock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond destroyedStock.errors
            return
        }

        try {
            destroyedStockService.save(destroyedStock)
        } catch (ValidationException e) {
            respond destroyedStock.errors
            return
        }

        respond destroyedStock, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || destroyedStockService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
