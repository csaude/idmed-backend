package mz.org.fgh.sifmoz.backend.prescriptionDetail

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PrescriptionDetailController extends RestfulController{

    IPrescriptionDetailService prescriptionDetailService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PrescriptionDetailController() {
        super(PrescriptionDetail)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(prescriptionDetailService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(prescriptionDetailService.get(id)) as JSON
    }

    @Transactional
    def save() {

        PrescriptionDetail prescriptionDetail = new PrescriptionDetail()
        def objectJSON = request.JSON
        prescriptionDetail = objectJSON as PrescriptionDetail

        prescriptionDetail.beforeInsert()
        prescriptionDetail.validate()

        if(objectJSON.id){
            prescriptionDetail.id = UUID.fromString(objectJSON.id)
        }

        if (prescriptionDetail.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond prescriptionDetail.errors
            return
        }

        try {
            prescriptionDetailService.save(prescriptionDetail)
        } catch (ValidationException e) {
            respond prescriptionDetail.errors
            return
        }

        respond prescriptionDetail, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PrescriptionDetail prescriptionDetail) {
        if (prescriptionDetail == null) {
            render status: NOT_FOUND
            return
        }
        if (prescriptionDetail.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond prescriptionDetail.errors
            return
        }

        try {
            prescriptionDetailService.save(prescriptionDetail)
        } catch (ValidationException e) {
            respond prescriptionDetail.errors
            return
        }

        respond prescriptionDetail, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || prescriptionDetailService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByPrescriptionId(String prescriptionId) {
        respond prescriptionDetailService.getAllByPrescriptionId(prescriptionId)
    }
}
