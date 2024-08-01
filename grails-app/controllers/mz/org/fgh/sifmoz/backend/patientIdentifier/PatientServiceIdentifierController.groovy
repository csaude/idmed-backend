package mz.org.fgh.sifmoz.backend.patientIdentifier

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.springframework.validation.BindingResult

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PatientServiceIdentifierController extends RestfulController{

    IPatientServiceIdentifierService patientServiceIdentifierService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientServiceIdentifierController () {
        super(PatientServiceIdentifier)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientServiceIdentifierService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(patientServiceIdentifierService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PatientServiceIdentifier patientServiceIdentifier = new PatientServiceIdentifier()
        def objectJSON = request.JSON
        patientServiceIdentifier = objectJSON as PatientServiceIdentifier

        patientServiceIdentifier.beforeInsert()
        patientServiceIdentifier.validate()

        if(objectJSON.id){
            patientServiceIdentifier.id = UUID.fromString(objectJSON.id)
//            patientServiceIdentifier.identifierType = IdentifierType.get(objectJSON.identifierType.id).lock()
//            patientServiceIdentifier.service = ClinicalService.get(objectJSON.service.id).lock()
//            patientServiceIdentifier.clinic = Clinic.get(objectJSON.clinic.id).lock()
//            patientServiceIdentifier.patient = Patient.get(objectJSON.patient.id).lock()
        }

        if (patientServiceIdentifier.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientServiceIdentifier.errors
            return
        }

        try {
            patientServiceIdentifierService.save(patientServiceIdentifier)
        } catch (ValidationException e) {
            respond patientServiceIdentifier.errors
            return
        }

        render JSONSerializer.setJsonObjectResponse(patientServiceIdentifier) as JSON
    }

    @Transactional
    def update( ) {
        def objectJSON = request.JSON
        PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.get(objectJSON.id)
        def patientServiceIdentifierFromJSON = (parseTo(objectJSON.toString()) as Map) as PatientServiceIdentifier

        bindData(patientServiceIdentifier, patientServiceIdentifierFromJSON, [exclude: ['id', 'clinicId', 'patientId', 'identifierTypeId', 'validated', 'serviceId', 'entity']])

        def syncStatus = objectJSON["syncStatus"]
        if (patientServiceIdentifier == null) {
            render status: NOT_FOUND
            return
        }
        patientServiceIdentifier.episodes.eachWithIndex { Episode episode, int i ->
            episode.id = UUID.fromString(objectJSON.episodes[i].id)
            episode.patientServiceIdentifier = patientServiceIdentifier
        }

        if (patientServiceIdentifier == null) {
            render status: NOT_FOUND
            return
        }

        if (patientServiceIdentifier.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientServiceIdentifier.errors
            return
        }

        try {
            patientServiceIdentifierService.save(patientServiceIdentifier)
        } catch (ValidationException e) {
            respond patientServiceIdentifier.errors
            return
        }

        def clinic = JSONSerializer.setJsonLightObjectResponse(patientServiceIdentifier.clinic)
        def patient = JSONSerializer.setJsonLightObjectResponse(patientServiceIdentifier.patient)
        def service = JSONSerializer.setJsonLightObjectResponse(patientServiceIdentifier.service)

        def result = JSONSerializer.setJsonObjectResponse(patientServiceIdentifier)
        if (clinic != null) {
            result.put('clinic', clinic)
        }

        if (patient != null) {
            result.put('patient', patient)
        }

        if (service != null) {
            result.put('service', service)
        }

        render result as JSON
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientServiceIdentifierService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond patientServiceIdentifierService.getAllByClinicId(clinicId, offset, max)
    }

    def getByPatientId(String patientId, int offset, int max) {
        def result = patientServiceIdentifierService.getAllByPatientId(patientId)
        render JSONSerializer.setObjectListJsonResponse(result) as JSON
    }

    def getByNidValue(String nidValue) {
        String replacedString = nidValue.replace("-", "/");
        def result = PatientServiceIdentifier.findByValue(replacedString)
         if (result != null)   render JSONSerializer.setJsonObjectResponse(result) as JSON
        render status: NO_CONTENT
    }

//    def getByServiceId(String serviceId) {
////        for (i in patientServiceIdentifierService.getAllByServiceId(serviceId)) {
////            System.out.println(i as JSON)
////        }
//
//        def result = patientServiceIdentifierService.getAllByPatientId(serviceId)
//
//        System.out.println(result.size())
//
//        render JSONSerializer.setObjectListJsonResponse(result) as JSON
//    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

}
