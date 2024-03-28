package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import jakarta.inject.Inject
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class DistrictController extends RestfulController{

    DistrictService districtService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DistrictController() {
        super(District)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
            render JSONSerializer.setObjectListJsonResponse(districtService.list(params)) as JSON
    }

    def show(String id) {
            render JSONSerializer.setJsonObjectResponse(districtService.get(id)) as JSON
    }

    @Transactional
    def save() {

        District district = new District()
        def objectJSON = request.JSON
        district = objectJSON as District

        district.beforeInsert()
        district.validate()

        if(objectJSON.id){
            district.id = UUID.fromString(objectJSON.id)
        }

        if (district.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond district.errors
            return
        }

        try {
            districtService.save(district)
        } catch (ValidationException e) {
            respond district.errors
            return
        }

        respond district, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(District district) {
        if (district == null) {
            render status: NOT_FOUND
            return
        }
        if (district.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond district.errors
            return
        }

        try {
            districtService.save(district)
        } catch (ValidationException e) {
            respond district.errors
            return
        }

        respond district, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || districtService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
