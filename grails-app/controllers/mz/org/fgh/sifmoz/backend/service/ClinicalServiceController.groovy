package mz.org.fgh.sifmoz.backend.service

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.clinicSectorType.ClinicSectorType
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.screening.AdherenceScreening
import mz.org.fgh.sifmoz.backend.screening.RAMScreening
import mz.org.fgh.sifmoz.backend.serviceattribute.ClinicalServiceAttribute
import mz.org.fgh.sifmoz.backend.serviceattribute.ClinicalServiceAttributeService
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimenService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.Transactional

class ClinicalServiceController extends RestfulController {

    ClinicalServiceService clinicalServiceService
    ClinicalServiceRestService clinicalServiceRestService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ClinicalServiceController() {
        super(ClinicalService)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(clinicalServiceService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(clinicalServiceService.get(id)) as JSON
    }

    @Transactional
    def save() {
        def objectJSON = request.JSON
        ClinicalService clinicalService = new ClinicalService(parseTo(objectJSON.toString()) as Map)

        clinicalService.beforeInsert()
        clinicalService.validate()

        if (objectJSON.id)
            clinicalService.id = UUID.fromString(objectJSON.id)

        if (clinicalService.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalService.errors
            return
        }

        try {
            clinicalServiceService.save(clinicalService)
        } catch (ValidationException e) {
            respond clinicalService.errors
            return
        }
        render JSONSerializer.setJsonObjectResponse(clinicalService) as JSON
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        ClinicalService clinicalService = ClinicalService.get(objectJSON.id)
        ClinicalService clinicalServiceAux = new ClinicalService(parseTo(objectJSON.toString()) as Map)
        bindData(clinicalService, clinicalServiceAux, [exclude: ['id', 'validated', 'identifierTypeId', 'tarv', 'prep', 'entity']])

        if (clinicalService.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicalService.errors
            return
        }

        clinicalService.clinicSectors = [].withDefault { new ClinicSector() }
        (objectJSON.clinicSectors as List).collect { item ->
            if (item) {
                def clinicSectorObject = ClinicSector.get(item.id)
                clinicSectorObject.facilityType = FacilityType.get(item.facilityTypeId)
                clinicSectorObject.parentClinic = Clinic.get(item.parentClinic_id)
                clinicalService.addToClinicSectors(clinicSectorObject)
            }
        }

        try {
            clinicalService.save(flush: true, failOnError: true)
        } catch (ValidationException e) {
            respond clinicalService.errors
            return
        }

        def result = JSONSerializer.setJsonObjectResponse(clinicalService)


        def clinicSectorJSON = JSONSerializer.setLightObjectListJsonResponse(clinicalService.clinicSectors as List)
        def therapeuticRegimensJSON = JSONSerializer.setLightObjectListJsonResponse(clinicalService.therapeuticRegimens as List)

        if (clinicSectorJSON.length() > 0) {
            result.put('clinicSectors', clinicSectorJSON)
        } else {
            result.remove('clinicSectors')
        }

        if (therapeuticRegimensJSON.length() > 0) {
            result.put('therapeuticRegimens', therapeuticRegimensJSON)
        } else {
            result.remove('therapeuticRegimens')
        }


        render result as JSON
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicalServiceService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def clinicSectorSaved(ClinicSector clinicSector) {
        ClinicSector.withTransaction {
            return clinicSector.save(flush: true)
        }
    }

    def getClinicalServiceFromProvincialServer(int offset) {
        render JSONSerializer.setLightObjectListJsonResponse(clinicalServiceRestService.loadClinicalServiceFromProvincial(offset)) as JSON
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }
}
