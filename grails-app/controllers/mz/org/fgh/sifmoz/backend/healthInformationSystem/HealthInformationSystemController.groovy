package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.grails.web.json.JSONObject
import org.springframework.validation.BindingResult

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

import grails.gorm.transactions.Transactional

class HealthInformationSystemController extends RestfulController{

    IHealthInformationSystemService healthInformationSystemService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    HealthInformationSystemController() {
        super(HealthInformationSystem)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(healthInformationSystemService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(healthInformationSystemService.get(id)) as JSON
    }

    @Transactional
    def save() {
        HealthInformationSystem healthInformationSystem = new HealthInformationSystem()
        def objectJSON = request.JSON
        healthInformationSystem = objectJSON as HealthInformationSystem

        healthInformationSystem.beforeInsert()
        healthInformationSystem.validate()

        if(objectJSON.id){
            healthInformationSystem.id = UUID.fromString(objectJSON.id)
            healthInformationSystem.interoperabilityAttributes.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.interoperabilityAttributes[index].id)
            }
        }
        if (healthInformationSystem.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond healthInformationSystem.errors
            return
        }

        try {
            healthInformationSystemService.save(healthInformationSystem)
        } catch (ValidationException e) {
            respond healthInformationSystem.errors
            return
        }

        def interoperabilityAttributesJSON = JSONSerializer.setObjectListJsonResponse(healthInformationSystem.interoperabilityAttributes as List)

        def result = JSONSerializer.setJsonObjectResponse(HealthInformationSystem.get(healthInformationSystem.id))
        if (interoperabilityAttributesJSON.length() > 0) {
            result.put('interoperabilityAttributes', interoperabilityAttributesJSON)

        }
        render result as JSON
    }

    //@Transactional
    def update() {
        HealthInformationSystem healthInformationSystem
        def objectJSON = request.JSON

        if(objectJSON.id) {
            healthInformationSystem = HealthInformationSystem.get(objectJSON.id)

            if (healthInformationSystem == null) {
                render status: NOT_FOUND
                return
            }

            healthInformationSystem.abbreviation = objectJSON.getAt('abbreviation')
            healthInformationSystem.description = objectJSON.getAt('description')
            healthInformationSystem.active = objectJSON.getAt('active') as boolean


        }
        HealthInformationSystem healthInformationSystemUpdated = healthInformationSystemService.helperUpdate(objectJSON as JSONObject)


        def result = JSONSerializer.setJsonObjectResponse(healthInformationSystemUpdated)
        def interoperabilityAttributesJSON = JSONSerializer.setObjectListJsonResponse(HealthInformationSystem.get(objectJSON.id).interoperabilityAttributes as List)

        if(interoperabilityAttributesJSON.length() > 0){
            result.put('interoperabilityAttributes', interoperabilityAttributesJSON)
        }else{
            result.remove('interoperabilityAttributesJSON')
        }
        render result as JSON
    }

    @Transactional
    def delete(String id) {
        if (id == null || healthInformationSystemService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}