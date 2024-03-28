package mz.org.fgh.sifmoz.backend.attributeType

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional


class PatientAttributeTypeController extends RestfulController {

    PatientAttributeTypeService attributeTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientAttributeTypeController() {
        super(PatientAttributeType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(attributeTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(attributeTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        PatientAttributeType attributeType = new PatientAttributeType()
        def objectJSON = request.JSON
        attributeType = objectJSON as PatientAttributeType

        attributeType.beforeInsert()
        attributeType.validate()

        if(objectJSON.id){
            attributeType.id = UUID.fromString(objectJSON.id)
        }

        if (attributeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond attributeType.errors
            return
        }

        try {
            attributeTypeService.save(attributeType)
        } catch (ValidationException e) {
            respond attributeType.errors
            return
        }

        respond attributeType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientAttributeType attributeType) {
        if (attributeType == null) {
            render status: NOT_FOUND
            return
        }
        if (attributeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond attributeType.errors
            return
        }

        try {
            attributeTypeService.save(attributeType)
        } catch (ValidationException e) {
            respond attributeType.errors
            return
        }

        respond attributeType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || attributeTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
