package mz.org.fgh.sifmoz.backend.startStopReason

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

class StartStopReasonController extends RestfulController{

    StartStopReasonService startStopReasonService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StartStopReasonController() {
        super(StartStopReason)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(startStopReasonService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(startStopReasonService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StartStopReason startStopReason = new StartStopReason()
        def objectJSON = request.JSON
        startStopReason = objectJSON as StartStopReason

        startStopReason.beforeInsert()
        startStopReason.validate()

        if(objectJSON.id){
            startStopReason.id = UUID.fromString(objectJSON.id)
        }
        if (startStopReason.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond startStopReason.errors
            return
        }

        try {
            startStopReasonService.save(startStopReason)
        } catch (ValidationException e) {
            respond startStopReason.errors
            return
        }

        respond startStopReason, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(StartStopReason startStopReason) {
        if (startStopReason == null) {
            render status: NOT_FOUND
            return
        }
        if (startStopReason.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond startStopReason.errors
            return
        }

        try {
            startStopReasonService.save(startStopReason)
        } catch (ValidationException e) {
            respond startStopReason.errors
            return
        }

        respond startStopReason, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || startStopReasonService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
