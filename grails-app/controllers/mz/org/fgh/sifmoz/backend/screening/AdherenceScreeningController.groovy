package mz.org.fgh.sifmoz.backend.screening

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class AdherenceScreeningController extends RestfulController{

    AdherenceScreeningService adherenceScreeningService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    AdherenceScreeningController() {
        super(AdherenceScreening)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(adherenceScreeningService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(adherenceScreeningService.get(id)) as JSON
    }

    @Transactional
    def save() {
        AdherenceScreening adherenceScreening = new AdherenceScreening()
        def objectJSON = request.JSON
        adherenceScreening = objectJSON as AdherenceScreening

        adherenceScreening.beforeInsert()
        adherenceScreening.validate()

        if(objectJSON.id){
            adherenceScreening.id = UUID.fromString(objectJSON.id)
        }
        if (adherenceScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond adherenceScreening.errors
            return
        }

        try {
            configAdherenceScreeningOrigin(adherenceScreening)
            adherenceScreeningService.save(adherenceScreening)
        } catch (ValidationException e) {
            respond adherenceScreening.errors
            return
        }

        respond adherenceScreening, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(AdherenceScreening adherenceScreening) {
        if (adherenceScreening == null) {
            render status: NOT_FOUND
            return
        }
        if (adherenceScreening.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond adherenceScreening.errors
            return
        }

        try {
            configAdherenceScreeningOrigin(adherenceScreening)
            adherenceScreeningService.save(adherenceScreening)
        } catch (ValidationException e) {
            respond adherenceScreening.errors
            return
        }

        respond adherenceScreening, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || adherenceScreeningService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    private static AdherenceScreening configAdherenceScreeningOrigin(AdherenceScreening adherenceScreening){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL") && checkHasNotOrigin(adherenceScreening)){
            adherenceScreening.origin = systemConfigs.description
        }

        return adherenceScreening
    }

    private static boolean checkHasNotOrigin(AdherenceScreening adherenceScreening){
        return adherenceScreening.origin == null || adherenceScreening?.origin?.isEmpty()
    }
}
