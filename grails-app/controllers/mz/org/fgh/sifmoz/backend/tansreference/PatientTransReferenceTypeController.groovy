package mz.org.fgh.sifmoz.backend.tansreference

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class PatientTransReferenceTypeController extends RestfulController{

    PatientTransReferenceTypeService patientTransReferenceTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientTransReferenceTypeController() {
        super(PatientTransReferenceType.class)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientTransReferenceTypeService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(patientTransReferenceTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PatientTransReferenceType patientTransReferenceType = new PatientTransReferenceType()
        def objectJSON = request.JSON
        patientTransReferenceType = objectJSON as PatientTransReferenceType

        patientTransReferenceType.beforeInsert()
        patientTransReferenceType.validate()

        if(objectJSON.id){
            patientTransReferenceType.id = UUID.fromString(objectJSON.id)
        }
        if (patientTransReferenceType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientTransReferenceType.errors
            return
        }

        try {
            patientTransReferenceTypeService.save(patientTransReferenceType)
        } catch (ValidationException e) {
            respond patientTransReferenceType.errors
            return
        }

        respond patientTransReferenceType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientTransReferenceType patientTransReferenceType) {
        if (patientTransReferenceType == null) {
            render status: NOT_FOUND
            return
        }
        if (patientTransReferenceType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientTransReferenceType.errors
            return
        }

        try {
            patientTransReferenceTypeService.save(patientTransReferenceType)
        } catch (ValidationException e) {
            respond patientTransReferenceType.errors
            return
        }

        respond patientTransReferenceType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientTransReferenceTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
