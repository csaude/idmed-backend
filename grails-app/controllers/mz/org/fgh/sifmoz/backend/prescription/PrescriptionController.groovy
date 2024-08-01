package mz.org.fgh.sifmoz.backend.prescription


import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.doctor.DoctorService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PrescriptionController extends RestfulController{

    IPrescriptionService prescriptionService
    DoctorService doctorService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PrescriptionController() {
        super(Prescription)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(prescriptionService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(prescriptionService.get(id)) as JSON
    }

    private getIncludeFields() {
        params.fields?.tokenize(',')
    }

    @Transactional
    def save() {
        Prescription prescription = new Prescription()
        def objectJSON = request.JSON
        prescription = objectJSON as Prescription

        prescription.beforeInsert()
        prescription.validate()

        if(objectJSON.id){
            prescription.id = UUID.fromString(objectJSON.id)
            prescription.prescribedDrugs.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.prescribedDrugs[index].id)
            }
            prescription.prescriptionDetails.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.prescriptionDetails[index].id)
            }
        }
        if (prescription.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond prescription.errors
            return
        }

        try {
            if (!Utilities.stringHasValue(prescription.id)) {
                prescription.generateNextSeq()
            }
            prescriptionService.save(prescription)
        } catch (ValidationException e) {
            respond prescription.errors
            return
        }

        respond prescription, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Prescription prescription) {
        if (prescription == null) {
            render status: NOT_FOUND
            return
        }
        if (prescription.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond prescription.errors
            return
        }

        try {
            prescriptionService.save(prescription)
        } catch (ValidationException e) {
            respond prescription.errors
            return
        }

        respond prescription, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || prescriptionService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByClinicId(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(prescriptionService.getAllByClinicId(clinicId, offset, max)) as JSON
        // respond prescriptionService.getAllByClinicId(clinicId, offset, max)
        /*JSON.use('deep'){
            render prescriptionService.getAllByClinicId(clinicId, offset, max) as JSON
        }*/
    }
    def getByVisitIds(String pvdsId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(prescriptionService.getByVisitId(pvdsId, offset, max)) as JSON
        // respond prescriptionService.getAllByClinicId(clinicId, offset, max)
        /*JSON.use('deep'){
            render prescriptionService.getAllByClinicId(clinicId, offset, max) as JSON
        }*/
    }

    // Futuramente reduzir para 2 ultimas prescricoes
    def getAllPrescriptionByPatientId(String patientId){
        def patient = Patient.get(patientId)
        def lastPatientVisit = PatientVisit.findAllByPatient(patient, [max: 3, sort: "visitDate", order:"desc"])
        def patientVisitDetails = PatientVisitDetails.findAllByPatientVisitInList(lastPatientVisit)
        def prescriptions = Prescription.findAllByIdInList(patientVisitDetails?.prescription?.id, [max: 3, sort: "prescriptionDate", order:"desc"])

        render JSONSerializer.setObjectListJsonResponse(prescriptions) as JSON
    }

    def getAllLastPrescriptionOfClinic(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(prescriptionService.getAllLastPrescriptionOfClinic(clinicId, offset, max)) as JSON
    }

    def getAllByPrescriptionIds() {
        def objectJSON = request.JSON
        List<String> ids = objectJSON
        render JSONSerializer.setObjectListJsonResponse(Prescription.findAllByIdInList(ids)) as JSON
    }
}
