package mz.org.fgh.sifmoz.backend.duration

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

class DurationController extends RestfulController{

    DurationService durationService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    public DurationController () {
        super(Duration)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(durationService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(durationService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Duration duration = new Duration()
        def objectJSON = request.JSON
        duration = objectJSON as Duration

        duration.beforeInsert()
        duration.validate()

        if(objectJSON.id){
            duration.id = UUID.fromString(objectJSON.id)
        }

        if (duration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond duration.errors
            return
        }

        try {
            durationService.save(duration)
        } catch (ValidationException e) {
            respond duration.errors
            return
        }

        respond duration, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Duration duration) {
        if (duration == null) {
            render status: NOT_FOUND
            return
        }
        if (duration.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond duration.errors
            return
        }

        try {
            durationService.save(duration)
        } catch (ValidationException e) {
            respond duration.errors
            return
        }

        respond duration, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || durationService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
