package mz.org.fgh.sifmoz.backend.identifierType

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class IdentifierTypeController extends RestfulController{

    IdentifierTypeService identifierTypeService
    IdentifierTypeRestService identifierTypeRestService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    IdentifierTypeController() {
        super(IdentifierType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(identifierTypeService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(identifierTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        IdentifierType identifierType = new IdentifierType()
        def objectJSON = request.JSON
        identifierType = objectJSON as IdentifierType

        identifierType.beforeInsert()
        identifierType.validate()

        if(objectJSON.id){
            identifierType.id = UUID.fromString(objectJSON.id)
        }
        if (identifierType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond identifierType.errors
            return
        }

        try {
            identifierTypeService.save(identifierType)
        } catch (ValidationException e) {
            respond identifierType.errors
            return
        }

        respond identifierType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {
        IdentifierType identifierType
        def objectJSON = request.JSON

        println(objectJSON)

        if(objectJSON.id){
            identifierType = IdentifierType.get(objectJSON.id)
            if (identifierType == null) {
                render status: NOT_FOUND
                return
            }
            identifierType.properties = objectJSON
        }

        if (identifierType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond identifierType.errors
            return
        }

        try {
            identifierTypeService.save(identifierType)
        } catch (ValidationException e) {
            respond identifierType.errors
            return
        }

        respond identifierType, [status: OK, view:"show"]

    }

    @Transactional
    def delete(Long id) {
        if (id == null || identifierTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getidentifierTypesFromProvincialServer(int offset) {
        render JSONSerializer.setObjectListJsonResponse(identifierTypeRestService.loadIdentifierTypesFromProvincial(offset)) as JSON
    }
}
