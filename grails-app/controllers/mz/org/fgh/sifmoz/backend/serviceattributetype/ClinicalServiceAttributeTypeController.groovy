package mz.org.fgh.sifmoz.backend.serviceattributetype

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import grails.gorm.transactions.Transactional

class ClinicalServiceAttributeTypeController extends RestfulController{

    ClinicalServiceAttributeTypeService clinicalServiceAttributeTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ClinicalServiceAttributeTypeController () {
        super(ClinicalServiceAttributeType)
    }
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(clinicalServiceAttributeTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(clinicalServiceAttributeTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        ClinicalServiceAttributeType clinicalServiceAttributeType = new ClinicalServiceAttributeType()
        def objectJSON = request.JSON
        clinicalServiceAttributeType = objectJSON as ClinicalServiceAttributeType

        clinicalServiceAttributeType.beforeInsert()
        clinicalServiceAttributeType.validate()

        if(objectJSON.id){
            clinicalServiceAttributeType.id = UUID.fromString(objectJSON.id)
        }

        if (clinicalServiceAttributeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalServiceAttributeType.errors
            return
        }

        try {
            clinicalServiceAttributeTypeService.save(clinicalServiceAttributeType)
        } catch (ValidationException e) {
            respond clinicalServiceAttributeType.errors
            return
        }

        respond clinicalServiceAttributeType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ClinicalServiceAttributeType clinicalServiceAttributeType) {
        if (clinicalServiceAttributeType == null) {
            render status: NOT_FOUND
            return
        }
        if (clinicalServiceAttributeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalServiceAttributeType.errors
            return
        }

        try {
            clinicalServiceAttributeTypeService.save(clinicalServiceAttributeType)
        } catch (ValidationException e) {
            respond clinicalServiceAttributeType.errors
            return
        }

        respond clinicalServiceAttributeType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicalServiceAttributeTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
