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

class VitalSignsScreeningController extends RestfulController{

    VitalSignsScreeningService vitalSignsScreeningService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    VitalSignsScreeningController() {
        super(VitalSignsScreening)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(vitalSignsScreeningService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(vitalSignsScreeningService.get(id)) as JSON
    }

    @Transactional
    def save() {
        VitalSignsScreening vitalSignsScreening = new VitalSignsScreening()
        def objectJSON = request.JSON
        vitalSignsScreening = objectJSON as VitalSignsScreening

        vitalSignsScreening.beforeInsert()
        vitalSignsScreening.validate()

        if(objectJSON.id){
            vitalSignsScreening.id = UUID.fromString(objectJSON.id)
        }
        if (vitalSignsScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond vitalSignsScreening.errors
            return
        }

        try {
            configVitalSignsScreeningOrigin(vitalSignsScreening)
            vitalSignsScreeningService.save(vitalSignsScreening)
        } catch (ValidationException e) {
            respond vitalSignsScreening.errors
            return
        }

        respond vitalSignsScreening, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(VitalSignsScreening vitalSignsScreening) {
        if (vitalSignsScreening == null) {
            render status: NOT_FOUND
            return
        }
        if (vitalSignsScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond vitalSignsScreening.errors
            return
        }

        try {
            configVitalSignsScreeningOrigin(vitalSignsScreening)
            vitalSignsScreeningService.save(vitalSignsScreening)
        } catch (ValidationException e) {
            respond vitalSignsScreening.errors
            return
        }

        respond vitalSignsScreening, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || vitalSignsScreeningService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    private static VitalSignsScreening configVitalSignsScreeningOrigin(VitalSignsScreening vitalSignsScreening){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL")){
            vitalSignsScreening.origin = systemConfigs.description
        }

        return vitalSignsScreening
    }

}
