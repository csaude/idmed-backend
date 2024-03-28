package mz.org.fgh.sifmoz.backend.prescription

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import grails.gorm.transactions.Transactional

class SpetialPrescriptionMotiveController extends RestfulController {

    SpetialPrescriptionMotiveService spetialPrescriptionMotiveService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    SpetialPrescriptionMotiveController() {
        super(SpetialPrescriptionMotive.class)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(spetialPrescriptionMotiveService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(spetialPrescriptionMotiveService.get(id)) as JSON
    }

    @Transactional
    def save() {
        SpetialPrescriptionMotive spetialPrescriptionMotive = new SpetialPrescriptionMotive()
        def objectJSON = request.JSON
        spetialPrescriptionMotive = objectJSON as SpetialPrescriptionMotive

        spetialPrescriptionMotive.beforeInsert()
        spetialPrescriptionMotive.validate()

        if(objectJSON.id){
            spetialPrescriptionMotive.id = UUID.fromString(objectJSON.id)
        }
        if (spetialPrescriptionMotive.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond spetialPrescriptionMotive.errors
            return
        }

        try {
            spetialPrescriptionMotiveService.save(spetialPrescriptionMotive)
        } catch (ValidationException e) {
            respond spetialPrescriptionMotive.errors
            return
        }

        respond spetialPrescriptionMotive, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(SpetialPrescriptionMotive spetialPrescriptionMotive) {
        if (spetialPrescriptionMotive == null) {
            render status: NOT_FOUND
            return
        }
        if (spetialPrescriptionMotive.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond spetialPrescriptionMotive.errors
            return
        }

        try {
            spetialPrescriptionMotiveService.save(spetialPrescriptionMotive)
        } catch (ValidationException e) {
            respond spetialPrescriptionMotive.errors
            return
        }

        respond spetialPrescriptionMotive, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || spetialPrescriptionMotiveService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
