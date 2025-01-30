package mz.org.fgh.sifmoz.backend.patientVisit

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK


class ExternalPatientVisitController extends RestfulController {

    ExternalPatientVisitService externalPatientVisitService

    ExternalPatientVisitController() {
        super(ExternalPatientVisit)
    }

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(externalPatientVisitService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(externalPatientVisitService.get(id)) as JSON
    }

    @Transactional

    def save() {
        ExternalPatientVisit externalPatientVisit = new ExternalPatientVisit()
        def objectJSON = request.JSON
        externalPatientVisit = objectJSON as ExternalPatientVisit

        externalPatientVisit.beforeInsert()
        externalPatientVisit.validate()

        if(objectJSON.id){
            externalPatientVisit.id = UUID.fromString(objectJSON.id)
        }
        if (externalPatientVisit.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond externalPatientVisit.errors
            return
        }

        try {
            externalPatientVisitService.save(externalPatientVisit)
        } catch (ValidationException e) {
            respond externalPatientVisit.errors
            return
        }

        respond externalPatientVisit, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ExternalPatientVisit externalPatientVisit) {
        if (externalPatientVisit == null) {
            render status: NOT_FOUND
            return
        }
        if (externalPatientVisit.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond externalPatientVisit.errors
            return
        }

        try {
            externalPatientVisitService.save(externalPatientVisit)
        } catch (ValidationException e) {
            respond externalPatientVisit.errors
            return
        }

        respond externalPatientVisit, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || externalPatientVisitService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}