package mz.org.fgh.sifmoz.backend.nationalClinic

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class NationalClinicController extends RestfulController{

    NationalClinicService nationalClinicService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    NationalClinicController() {
        super(NationalClinic)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(nationalClinicService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(nationalClinicService.get(id)) as JSON
    }

    @Transactional
    def save() {

        NationalClinic nationalClinic = new NationalClinic()
        def objectJSON = request.JSON
        nationalClinic = objectJSON as NationalClinic

        nationalClinic.beforeInsert()
        nationalClinic.validate()

        if(objectJSON.id){
            nationalClinic.id = UUID.fromString(objectJSON.id)
        }

        if (nationalClinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond nationalClinic.errors
            return
        }

        try {
            nationalClinicService.save(nationalClinic)
        } catch (ValidationException e) {
            respond nationalClinic.errors
            return
        }

        respond nationalClinic, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(NationalClinic nationalClinic) {
        if (nationalClinic == null) {
            render status: NOT_FOUND
            return
        }
        if (nationalClinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond nationalClinic.errors
            return
        }

        try {
            nationalClinicService.save(nationalClinic)
        } catch (ValidationException e) {
            respond nationalClinic.errors
            return
        }

        respond nationalClinic, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || nationalClinicService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
