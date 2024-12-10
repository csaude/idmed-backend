package mz.org.fgh.sifmoz.backend.screening

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class PregnancyScreeningController extends RestfulController{

    PregnancyScreeningService pregnancyScreeningService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PregnancyScreeningController() {
        super(PregnancyScreening)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(pregnancyScreeningService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(pregnancyScreeningService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PregnancyScreening pregnancyScreening = new PregnancyScreening()
        def objectJSON = request.JSON
        pregnancyScreening = objectJSON as PregnancyScreening

        pregnancyScreening.beforeInsert()
        pregnancyScreening.validate()

        if(objectJSON.id){
            pregnancyScreening.id = UUID.fromString(objectJSON.id)
        }

        if (pregnancyScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond pregnancyScreening.errors
            return
        }

        try {
            configPregnancyScreeningOrigin(pregnancyScreening)
            pregnancyScreeningService.save(pregnancyScreening)
        } catch (ValidationException e) {
            respond pregnancyScreening.errors
            return
        }

        respond pregnancyScreening, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PregnancyScreening pregnancyScreening) {
        if (pregnancyScreening == null) {
            render status: NOT_FOUND
            return
        }
        if (pregnancyScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond pregnancyScreening.errors
            return
        }

        try {
            configPregnancyScreeningOrigin(pregnancyScreening)
            pregnancyScreeningService.save(pregnancyScreening)
        } catch (ValidationException e) {
            respond pregnancyScreening.errors
            return
        }

        respond pregnancyScreening, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || pregnancyScreeningService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByPatientVisit(String patientVisitId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(PregnancyScreening.findAllByVisit(PatientVisit.findById(patientVisitId))) as JSON
    }

    private static PregnancyScreening configPregnancyScreeningOrigin(PregnancyScreening pregnancyScreening){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL") && checkHasNotOrigin(pregnancyScreening)){
            pregnancyScreening.origin = systemConfigs.description
        }

        return pregnancyScreening
    }

    private static boolean checkHasNotOrigin(PregnancyScreening pregnancyScreening){
        return pregnancyScreening.origin == null || pregnancyScreening?.origin?.isEmpty()
    }
}
