package mz.org.fgh.sifmoz.backend.clinicSectorType

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class ClinicSectorTypeController {

    ClinicSectorTypeService clinicSectorTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond clinicSectorTypeService.list(params), model:[clinicSectorTypeCount: clinicSectorTypeService.count()]
    }

    def show(Long id) {
        respond clinicSectorTypeService.get(id)
    }

    @Transactional
    def save() {

        ClinicSectorType clinicSectorType = new ClinicSectorType()
        def objectJSON = request.JSON
        clinicSectorType = objectJSON as ClinicSectorType

        clinicSectorType.beforeInsert()
        clinicSectorType.validate()

        if(objectJSON.id){
            clinicSectorType.id = UUID.fromString(objectJSON.id)
        }

        if (clinicSectorType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicSectorType.errors
            return
        }

        try {
            clinicSectorTypeService.save(clinicSectorType)
        } catch (ValidationException e) {
            respond clinicSectorType.errors
            return
        }

        respond clinicSectorType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ClinicSectorType clinicSectorType) {
        if (clinicSectorType == null) {
            render status: NOT_FOUND
            return
        }
        if (clinicSectorType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinicSectorType.errors
            return
        }

        try {
            clinicSectorTypeService.save(clinicSectorType)
        } catch (ValidationException e) {
            respond clinicSectorType.errors
            return
        }

        respond clinicSectorType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicSectorTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
