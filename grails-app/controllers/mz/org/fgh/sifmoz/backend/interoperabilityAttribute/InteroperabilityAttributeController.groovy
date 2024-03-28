package mz.org.fgh.sifmoz.backend.interoperabilityAttribute

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class InteroperabilityAttributeController extends RestfulController{

    InteroperabilityAttributeService interoperabilityAttributeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    InteroperabilityAttributeController() {
        super(InteroperabilityAttribute)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(interoperabilityAttributeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(interoperabilityAttributeService.get(id)) as JSON
    }

    @Transactional
    def save(I) {
        InteroperabilityAttribute interoperabilityAttribute = new InteroperabilityAttribute()
        def objectJSON = request.JSON
        interoperabilityAttribute = objectJSON as InteroperabilityAttribute

        interoperabilityAttribute.beforeInsert()
        interoperabilityAttribute.validate()

        if(objectJSON.id){
            interoperabilityAttribute.id = UUID.fromString(objectJSON.id)
        }
        if (interoperabilityAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond interoperabilityAttribute.errors
            return
        }

        try {
            interoperabilityAttributeService.save(interoperabilityAttribute)
        } catch (ValidationException e) {
            respond interoperabilityAttribute.errors
            return
        }

        respond interoperabilityAttribute, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(InteroperabilityAttribute interoperabilityAttribute) {
        if (interoperabilityAttribute == null) {
            render status: NOT_FOUND
            return
        }
        if (interoperabilityAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond interoperabilityAttribute.errors
            return
        }

        try {
            interoperabilityAttributeService.save(interoperabilityAttribute)
        } catch (ValidationException e) {
            respond interoperabilityAttribute.errors
            return
        }

        respond interoperabilityAttribute, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || interoperabilityAttributeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
