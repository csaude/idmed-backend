package mz.org.fgh.sifmoz.backend.dispenseType

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

class DispenseTypeController extends RestfulController{

    DispenseTypeService dispenseTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DispenseTypeController() {
        super(DispenseType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(dispenseTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(dispenseTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        DispenseType dispenseType = new DispenseType()
        def objectJSON = request.JSON
        dispenseType = objectJSON as DispenseType

        dispenseType.beforeInsert()
        dispenseType.validate()

        if(objectJSON.id){
            dispenseType.id = UUID.fromString(objectJSON.id)
        }

        if (dispenseType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond dispenseType.errors
            return
        }

        try {
            dispenseTypeService.save(dispenseType)
        } catch (ValidationException e) {
            respond dispenseType.errors
            return
        }

        respond dispenseType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(DispenseType dispenseType) {
        if (dispenseType == null) {
            render status: NOT_FOUND
            return
        }
        if (dispenseType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond dispenseType.errors
            return
        }

        try {
            dispenseTypeService.save(dispenseType)
        } catch (ValidationException e) {
            respond dispenseType.errors
            return
        }

        respond dispenseType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || dispenseTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
