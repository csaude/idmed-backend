package mz.org.fgh.sifmoz.backend.stockrefered

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.*

class ReferedStockMovimentController extends RestfulController{

    ReferedStockMovimentService referedStockMovimentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ReferedStockMovimentController() {
        super(ReferedStockMoviment)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(referedStockMovimentService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(referedStockMovimentService.get(id)) as JSON
    }

    @Transactional
    def save() {
        ReferedStockMoviment referedStockMoviment = new ReferedStockMoviment()
        def objectJSON = request.JSON
        referedStockMoviment = objectJSON as ReferedStockMoviment

        referedStockMoviment.beforeInsert()
        referedStockMoviment.adjustments.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.adjustments[index].id)
        }
        referedStockMoviment.validate()

        if(objectJSON.id){
            referedStockMoviment.id = UUID.fromString(objectJSON.id)
        }
        if (referedStockMoviment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referedStockMoviment.errors
            return
        }

        try {
            referedStockMovimentService.save(referedStockMoviment)
        } catch (ValidationException e) {
            respond referedStockMoviment.errors
            return
        }

        respond referedStockMoviment, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ReferedStockMoviment referedStockMoviment) {
        if (referedStockMoviment == null) {
            render status: NOT_FOUND
            return
        }
        if (referedStockMoviment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond referedStockMoviment.errors
            return
        }

        try {
            referedStockMovimentService.save(referedStockMoviment)
        } catch (ValidationException e) {
            respond referedStockMoviment.errors
            return
        }

        respond referedStockMoviment, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || referedStockMovimentService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond ReferedStockMoviment.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }
}
