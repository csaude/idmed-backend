package mz.org.fgh.sifmoz.backend.serviceattribute

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import grails.gorm.transactions.Transactional

class ClinicalServiceAttributeController extends RestfulController{

    ClinicalServiceAttributeService clinicalServiceAttributeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ClinicalServiceAttributeController() {
        super(ClinicalServiceAttribute)
    }
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(clinicalServiceAttributeService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(clinicalServiceAttributeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        ClinicalServiceAttribute clinicalServiceAttribute = new ClinicalServiceAttribute()
        def objectJSON = request.JSON
        clinicalServiceAttribute = objectJSON as ClinicalServiceAttribute

        clinicalServiceAttribute.beforeInsert()
        clinicalServiceAttribute.validate()

        if(objectJSON.id){
            clinicalServiceAttribute.id = UUID.fromString(objectJSON.id)
        }
        if (clinicalServiceAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalServiceAttribute.errors
            return
        }

        try {
            clinicalServiceAttributeService.save(clinicalServiceAttribute)
        } catch (ValidationException e) {
            respond clinicalServiceAttribute.errors
            return
        }

        respond clinicalServiceAttribute, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ClinicalServiceAttribute clinicalServiceAttribute) {
        if (clinicalServiceAttribute == null) {
            render status: NOT_FOUND
            return
        }
        if (clinicalServiceAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalServiceAttribute.errors
            return
        }

        try {
            clinicalServiceAttributeService.save(clinicalServiceAttribute)
        } catch (ValidationException e) {
            respond clinicalServiceAttribute.errors
            return
        }

        respond clinicalServiceAttribute, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicalServiceAttributeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
