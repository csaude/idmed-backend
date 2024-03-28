package mz.org.fgh.sifmoz.backend.openmrsErrorLog

import grails.rest.RestfulController
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class OpenmrsErrorLogController extends RestfulController{

    OpenmrsErrorLogService openmrsErrorLogService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    OpenmrsErrorLogController() {
        super(OpenmrsErrorLog)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond openmrsErrorLogService.list(params), model:[openmrsErrorLogCount: openmrsErrorLogService.count()]
    }

    def show(Long id) {
        respond openmrsErrorLogService.get(id)
    }

    @Transactional
    def save(OpenmrsErrorLog openmrsErrorLog) {
        if (openmrsErrorLog == null) {
            render status: NOT_FOUND
            return
        }
        if (openmrsErrorLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond openmrsErrorLog.errors
            return
        }

        try {
            openmrsErrorLogService.save(openmrsErrorLog)
        } catch (ValidationException e) {
            respond openmrsErrorLog.errors
            return
        }

        respond openmrsErrorLog, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(OpenmrsErrorLog openmrsErrorLog) {
        if (openmrsErrorLog == null) {
            render status: NOT_FOUND
            return
        }
        if (openmrsErrorLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond openmrsErrorLog.errors
            return
        }

        try {
            openmrsErrorLogService.save(openmrsErrorLog)
        } catch (ValidationException e) {
            respond openmrsErrorLog.errors
            return
        }

        respond openmrsErrorLog, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || openmrsErrorLogService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
