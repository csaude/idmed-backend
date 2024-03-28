package mz.org.fgh.sifmoz.backend.provincialServer

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class ProvincialServerController {

    IProvincialServerService provincialServerService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond provincialServerService.list(params), model:[provincialServerCount: provincialServerService.count()]
    }

    def show(Long id) {
        respond provincialServerService.get(id)
    }

    @Transactional
    def save() {
        ProvincialServer provincialServer = new ProvincialServer()
        def objectJSON = request.JSON
        provincialServer = objectJSON as ProvincialServer

        provincialServer.beforeInsert()
        provincialServer.validate()

        if(objectJSON.id){
            provincialServer.id = UUID.fromString(objectJSON.id)
        }
        if (provincialServer.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond provincialServer.errors
            return
        }

        try {
            provincialServerService.save(provincialServer)
        } catch (ValidationException e) {
            respond provincialServer.errors
            return
        }

        respond provincialServer, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(ProvincialServer provincialServer) {
        if (provincialServer == null) {
            render status: NOT_FOUND
            return
        }
        if (provincialServer.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond provincialServer.errors
            return
        }

        try {
            provincialServerService.save(provincialServer)
        } catch (ValidationException e) {
            respond provincialServer.errors
            return
        }

        respond provincialServer, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || provincialServerService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
