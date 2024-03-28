package mz.org.fgh.sifmoz.backend.facilityType

import grails.converters.JSON
import grails.persistence.Event
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

class FacilityTypeController extends RestfulController{

    FacilityTypeService facilityTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    FacilityTypeController() {
        super(FacilityType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(facilityTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(facilityTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        FacilityType facilityType = new FacilityType()
        def objectJSON = request.JSON
        facilityType = objectJSON as FacilityType

        facilityType.beforeInsert()
        facilityType.validate()

        if(objectJSON.id){
            facilityType.id = UUID.fromString(objectJSON.id)
        }

        if (facilityType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond facilityType.errors
            return
        }

        try {
            facilityTypeService.save(facilityType)
        } catch (ValidationException e) {
            respond facilityType.errors
            return
        }

        respond facilityType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(FacilityType facilityType) {
        if (facilityType == null) {
            render status: NOT_FOUND
            return
        }
        if (facilityType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond facilityType.errors
            return
        }

        try {
            facilityTypeService.save(facilityType)
        } catch (ValidationException e) {
            respond facilityType.errors
            return
        }

        respond facilityType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || facilityTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
