package mz.org.fgh.sifmoz.backend.stockrefered

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

class ReferedStockMovimentController extends RestfulController{

    ReferedStockMovimentService referedStockMovimentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ReferedStockMovimentController() {
        super(ReferedStockMoviment)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(referedStockMovimentService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(referedStockMovimentService.get(id)) as JSON
    }

    @Transactional
    def save() {
        ReferedStockMoviment referedStockMoviment = new ReferedStockMoviment()
        def objectJSON = request.JSON
        referedStockMoviment = objectJSON as ReferedStockMoviment

        referedStockMoviment.beforeInsert()
        referedStockMoviment.adjustments.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.adjustments[index].id)
        }
        referedStockMoviment.validate()

        if(objectJSON.id){
            referedStockMoviment.id = UUID.fromString(objectJSON.id)
        }
        if (referedStockMoviment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referedStockMoviment.errors
            return
        }

        try {
            referedStockMovimentService.save(referedStockMoviment)
        } catch (ValidationException e) {
            respond referedStockMoviment.errors
            return
        }

        respond referedStockMoviment, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ReferedStockMoviment referedStockMoviment) {
        if (referedStockMoviment == null) {
            render status: NOT_FOUND
            return
        }
        if (referedStockMoviment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referedStockMoviment.errors
            return
        }

        try {
            referedStockMovimentService.save(referedStockMoviment)
        } catch (ValidationException e) {
            respond referedStockMoviment.errors
            return
        }

        respond referedStockMoviment, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || referedStockMovimentService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
