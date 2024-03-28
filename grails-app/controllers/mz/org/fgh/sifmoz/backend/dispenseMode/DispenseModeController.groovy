package mz.org.fgh.sifmoz.backend.dispenseMode

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

class DispenseModeController extends RestfulController{

    DispenseModeService dispenseModeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DispenseModeController() {
        super(DispenseMode)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(dispenseModeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(dispenseModeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        DispenseMode dispenseMode = new DispenseMode()
        def objectJSON = request.JSON
        dispenseMode = objectJSON as DispenseMode

        dispenseMode.beforeInsert()
        dispenseMode.validate()

        if(objectJSON.id){
            dispenseMode.id = UUID.fromString(objectJSON.id)
        }

        if (dispenseMode.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond dispenseMode.errors
            return
        }

        try {
            dispenseModeService.save(dispenseMode)
        } catch (ValidationException e) {
            respond dispenseMode.errors
            return
        }

        respond dispenseMode, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(DispenseMode dispenseMode) {
        if (dispenseMode == null) {
            render status: NOT_FOUND
            return
        }
        if (dispenseMode.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond dispenseMode.errors
            return
        }

        try {
            dispenseModeService.save(dispenseMode)
        } catch (ValidationException e) {
            respond dispenseMode.errors
            return
        }

        respond dispenseMode, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || dispenseModeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
