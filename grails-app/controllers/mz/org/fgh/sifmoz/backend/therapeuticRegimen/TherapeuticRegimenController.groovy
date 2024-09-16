package mz.org.fgh.sifmoz.backend.therapeuticRegimen

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class TherapeuticRegimenController extends RestfulController{

    TherapeuticRegimenService therapeuticRegimenService
    TherapeuticRegimenRestService therapeuticRegimenRestService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    TherapeuticRegimenController() {
        super(TherapeuticRegimen)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
            render JSONSerializer.setObjectListJsonResponse(therapeuticRegimenService.list(params)) as JSON
    }

    def show(String id) {
            render JSONSerializer.setJsonObjectResponse(therapeuticRegimenService.get(id)) as JSON
    }

    @Transactional
    def save() {
        TherapeuticRegimen therapeuticRegimen = new TherapeuticRegimen()
        def objectJSON = request.JSON
        therapeuticRegimen = objectJSON as TherapeuticRegimen

        therapeuticRegimen.beforeInsert()
        therapeuticRegimen.validate()

        if(objectJSON.id){
            province.id = UUID.fromString(objectJSON.id)
        }
        if (therapeuticRegimen.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond therapeuticRegimen.errors
            return
        }

        try {
            therapeuticRegimenService.save(therapeuticRegimen)
        } catch (ValidationException e) {
            respond therapeuticRegimen.errors
            return
        }

        respond therapeuticRegimen, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {
        TherapeuticRegimen therapeuticRegimen
        def objectJSON = request.JSON

        if(objectJSON.id){
            therapeuticRegimen = TherapeuticRegimen.get(objectJSON.id)
            if (therapeuticRegimen == null) {
                render status: NOT_FOUND
                return
            }
            therapeuticRegimen.properties = objectJSON
        }

        if (therapeuticRegimen.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond therapeuticRegimen.errors
            return
        }

        try {
            therapeuticRegimenService.save(therapeuticRegimen)
        } catch (ValidationException e) {
            respond therapeuticRegimen.errors
            return
        }

        respond therapeuticRegimen, [status: OK, view:"show"]

    }

    @Transactional
    def delete(Long id) {
        if (id == null || therapeuticRegimenService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getTherapeuticRegimensFromProvincialServer(int offset) {
        render JSONSerializer.setLightObjectListJsonResponse(therapeuticRegimenRestService.loadTherapeuticRegimenFromProvincial(offset)) as JSON
    }
}
