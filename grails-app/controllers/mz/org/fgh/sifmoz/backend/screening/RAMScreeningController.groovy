package mz.org.fgh.sifmoz.backend.screening

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class RAMScreeningController extends RestfulController{

    RAMScreeningService RAMScreeningService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    RAMScreeningController() {
        super(RAMScreening)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(RAMScreeningService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(RAMScreeningService.get(id)) as JSON
    }

    @Transactional
    def save() {
        RAMScreening rAMScreening = new RAMScreening()
        def objectJSON = request.JSON
        rAMScreening = objectJSON as RAMScreening

        rAMScreening.beforeInsert()
        rAMScreening.validate()

        if(objectJSON.id){
            rAMScreening.id = UUID.fromString(objectJSON.id)
        }
        if (rAMScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond rAMScreening.errors
            return
        }

        try {
            configRAMScreeningOrigin(rAMScreening)
            RAMScreeningService.save(rAMScreening)
        } catch (ValidationException e) {
            respond rAMScreening.errors
            return
        }

        respond rAMScreening, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(RAMScreening rAMScreening) {
        if (RAMScreening == null) {
            render status: NOT_FOUND
            return
        }
        if (rAMScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond rAMScreening.errors
            return
        }

        try {
            configRAMScreeningOrigin(rAMScreening)
            RAMScreeningService.save(rAMScreening)
        } catch (ValidationException e) {
            respond rAMScreening.errors
            return
        }

        respond rAMScreening, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || RAMScreeningService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    private static RAMScreening configRAMScreeningOrigin(RAMScreening ramScreening){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL")){
            ramScreening.origin = systemConfigs.description
        }

        return ramScreening
    }
}
