package mz.org.fgh.sifmoz.backend.tansreference

import com.google.gson.Gson
import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CONFLICT
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PatientTransReferenceController extends RestfulController{

    IPatientTransReferenceService patientTransReferenceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientTransReferenceController() {
        super(PatientTransReference.class)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientTransReferenceService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(patientTransReferenceService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PatientTransReference patientTransReference = new PatientTransReference()
        def objectJSON = request.JSON
        patientTransReference = objectJSON as PatientTransReference

        patientTransReference.beforeInsertId()
        patientTransReference.validate()

        if(objectJSON.id){
            patientTransReference.id = UUID.fromString(objectJSON.id)
        }
        if (patientTransReference.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientTransReference.errors
            return
        }

        try {
            patientTransReferenceService.save(patientTransReference)
        } catch (ValidationException e) {
            respond patientTransReference.errors
            return
        }

        respond patientTransReference, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientTransReference patientTransReference) {
        if (patientTransReference == null) {
            render status: NOT_FOUND
            return
        }
        if (patientTransReference.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientTransReference.errors
            return
        }

        try {
            patientTransReferenceService.save(patientTransReference)
        } catch (ValidationException e) {
            respond patientTransReference.errors
            return
        }

        respond patientTransReference, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientTransReferenceService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getDetailsByNid(String nid) {
      //  render JSONSerializer.setObjectListJsonResponse(prescriptionService.getAllByClinicId(clinicId, offset, max)) as JSON
     //   Gson gson = new Gson();
   //    render gson.toJson(patientTransReferenceService.getPatientTransReferenceDetailsByNid(nid)) as JSON
      render JSONSerializer.setJsonObjectResponse(patientTransReferenceService.getPatientTransReferenceDetailsByNid(nid)) as JSON
    //      render  patientTransReferenceService.getPatientTransReferenceDetailsByNid(nid) as JSON

    }

    @Transactional
    def saveReferenceLostFolowUp(){


        def objectJSON = request.JSON
        def clinic = Clinic.findByMainClinic(true)
        def patientIdentifier = PatientServiceIdentifier.findByValue(objectJSON.identifier)
        def lastPickupString = ConvertDateUtils.convertDDMMYYYYToYYYYMMDD(objectJSON.lastPickup)
         def patientTransreferenceExists = PatientTransReference.findByIdentifierAndOperationDate(patientIdentifier , lastPickupString)
       if (patientTransreferenceExists != null)  {
           render status: CONFLICT
       } else {
        PatientTransReference patientTransReference = new PatientTransReference()
        patientTransReference.beforeInsertId()
        patientTransReference.syncStatus = 'P'
        patientTransReference.operationDate =  lastPickupString != null ? lastPickupString : new Date()
        patientTransReference.creationDate = new Date()
        patientTransReference.operationType =  PatientTransReferenceType.findByCode("REFERENCIA_DC")
        patientTransReference.origin = clinic
        patientTransReference.destination = clinic.id
        patientTransReference.patient = patientIdentifier?.patient
        patientTransReference.identifier = patientIdentifier
        patientTransReference.patientStatus = objectJSON.type

        patientTransReference.validate()

        if (patientTransReference.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientTransReference.errors
            return
        }

        try {
            patientTransReferenceService.save(patientTransReference)
        } catch (ValidationException e) {
            respond patientTransReference.errors
            return
        }
        respond patientTransReference, [status: CREATED, view:"show"]
       }
    }
}
