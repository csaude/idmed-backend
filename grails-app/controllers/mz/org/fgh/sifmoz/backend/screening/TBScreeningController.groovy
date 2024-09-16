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

class TBScreeningController extends RestfulController{

    TBScreeningService TBScreeningService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    TBScreeningController() {
        super(TBScreening)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(TBScreeningService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(TBScreeningService.get(id)) as JSON
    }

    @Transactional
    def save() {
        TBScreening tBScreening = new TBScreening()
        def objectJSON = request.JSON
        tBScreening = objectJSON as TBScreening

        tBScreening.beforeInsert()
        tBScreening.validate()

        if(objectJSON.id){
            tBScreening.id = UUID.fromString(objectJSON.id)
        }
        if (tBScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond tBScreening.errors
            return
        }

        try {
            configTBScreeningOrigin(tBScreening)
            TBScreeningService.save(tBScreening)
        } catch (ValidationException e) {
            respond tBScreening.errors
            return
        }

        respond tBScreening, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(TBScreening tBScreening) {
        if (TBScreening == null) {
            render status: NOT_FOUND
            return
        }
        if (tBScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond tBScreening.errors
            return
        }

        try {
            configTBScreeningOrigin(tBScreening)
            TBScreeningService.save(tBScreening)
        } catch (ValidationException e) {
            respond tBScreening.errors
            return
        }

        respond tBScreening, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || TBScreeningService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    private static TBScreening configTBScreeningOrigin(TBScreening tbScreening){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL")){
            tbScreening.origin = systemConfigs.description
        }

        return tbScreening
    }
}
