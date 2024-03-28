package mz.org.fgh.sifmoz.backend.serviceProgram

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional


class PatientProgramController extends RestfulController{

    PatientProgramService patientProgramService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientProgramController(Class resource) {
        super(resource)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientProgramService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(patientProgramService.get(id)) as JSON
    }

    @Transactional
    def save() {

        ServicePatient patientProgram = new ServicePatient()
        def objectJSON = request.JSON
        patientProgram = objectJSON as ServicePatient

        patientProgram.beforeInsert()
        patientProgram.validate()

        if (patientProgram.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientProgram.errors
            return
        }

        try {
            patientProgramService.save(patientProgram)
        } catch (ValidationException e) {
            respond patientProgram.errors
            return
        }

        respond patientProgram, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ServicePatient patientProgram) {
        if (patientProgram == null) {
            render status: NOT_FOUND
            return
        }
        if (patientProgram.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientProgram.errors
            return
        }

        try {
            patientProgramService.save(patientProgram)
        } catch (ValidationException e) {
            respond patientProgram.errors
            return
        }

        respond patientProgram, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientProgramService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
