package mz.org.fgh.sifmoz.backend.therapeuticLine

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

class TherapeuticLineController extends RestfulController{

    TherapeuticLineService therapeuticLineService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    TherapeuticLineController() {
        super(TherapeuticLine)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(therapeuticLineService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(therapeuticLineService.get(id)) as JSON
    }

    @Transactional
    def save() {
        TherapeuticLine therapeuticLine = new TherapeuticLine()
        def objectJSON = request.JSON
        therapeuticLine = objectJSON as TherapeuticLine

        therapeuticLine.beforeInsert()
        therapeuticLine.validate()

        if(objectJSON.id){
            therapeuticLine.id = UUID.fromString(objectJSON.id)
        }
        if (therapeuticLine.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond therapeuticLine.errors
            return
        }

        try {
            therapeuticLineService.save(therapeuticLine)
        } catch (ValidationException e) {
            respond therapeuticLine.errors
            return
        }

        respond therapeuticLine, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(TherapeuticLine therapeuticLine) {
        if (therapeuticLine == null) {
            render status: NOT_FOUND
            return
        }
        if (therapeuticLine.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond therapeuticLine.errors
            return
        }

        try {
            therapeuticLineService.save(therapeuticLine)
        } catch (ValidationException e) {
            respond therapeuticLine.errors
            return
        }

        respond therapeuticLine, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || therapeuticLineService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
