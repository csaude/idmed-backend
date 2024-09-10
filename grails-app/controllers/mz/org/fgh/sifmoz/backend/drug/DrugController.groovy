package mz.org.fgh.sifmoz.backend.drug

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class DrugController extends RestfulController{

    DrugService drugService
    DrugRestService drugRestService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DrugController() {
        super(Drug)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond drugService.list(params)
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(drugService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Drug drug = new Drug()
        def objectJSON = request.JSON
        drug = objectJSON as Drug

        drug.beforeInsert()
        drug.validate()

        if(objectJSON.id){
            drug.id = UUID.fromString(objectJSON.id)
        }

        if (drug.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond drug.errors
            return
        }

        try {
            drugService.save(drug)
        } catch (ValidationException e) {
            respond drug.errors
            return
        }

        respond drug, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {
        Drug drug
        def objectJSON = request.JSON
        println(objectJSON)
        if(objectJSON.id){
            drug = Drug.get(objectJSON.id)
            if (drug == null) {
                render status: NOT_FOUND
                return
            }
            drug.properties = objectJSON
        }

        if (drug.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond drug.errors
            return
        }

        try {
            drugService.save(drug)
        } catch (ValidationException e) {
            respond drug.errors
            return
        }

        respond drug, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || drugService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }
        render status: NO_CONTENT
    }

    def getDrugsFromProvincialServer(int offset) {
        render JSONSerializer.setLightObjectListJsonResponse(drugRestService.loadDrugsFromProvincial(offset)) as JSON
    }

    def getDrugsByInventoryId(String id) {
        render JSONSerializer.setObjectListJsonResponse(drugRestService.getDrugsByInventoryId(id)) as JSON
    }
}
