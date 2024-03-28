package mz.org.fgh.sifmoz.backend.patientAttribute

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

class PatientAttributeController extends RestfulController{

    PatientAttributeService patientAttributeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientAttributeController() {
        super(PatientAttribute)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientAttributeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(patientAttributeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PatientAttribute patientAttribute = new PatientAttribute()
        def objectJSON = request.JSON
        patientAttribute = objectJSON as PatientAttribute

        patientAttribute.beforeInsert()
        patientAttribute.validate()

        if(objectJSON.id){
            patientAttribute.id = UUID.fromString(objectJSON.id)
        }
        if (patientAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientAttribute.errors
            return
        }

        try {
            patientAttributeService.save(patientAttribute)
        } catch (ValidationException e) {
            respond patientAttribute.errors
            return
        }

        respond patientAttribute, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientAttribute patientAttribute) {
        if (patientAttribute == null) {
            render status: NOT_FOUND
            return
        }
        if (patientAttribute.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientAttribute.errors
            return
        }

        try {
            patientAttributeService.save(patientAttribute)
        } catch (ValidationException e) {
            respond patientAttribute.errors
            return
        }

        respond patientAttribute, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientAttributeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
