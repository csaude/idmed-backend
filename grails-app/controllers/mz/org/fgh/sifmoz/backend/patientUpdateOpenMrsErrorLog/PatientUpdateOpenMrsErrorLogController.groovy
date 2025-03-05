package mz.org.fgh.sifmoz.backend.patientUpdateOpenMrsErrorLog

import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLogService

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

class PatientUpdateOpenMrsErrorLogController {

    IPatientUpdateOpenMrsErrorLogControllerService patientUpdateOpenMrsErrorLogControllerService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientUpdateOpenMrsErrorLogController() {
        super(PatientUpdateOpenMrsErrorLog)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond patientUpdateOpenMrsErrorLogControllerService.list(params), model:[openmrsErrorLogCount: patientUpdateOpenMrsErrorLogControllerService.count()]
    }

    def show(Long id) {
        respond patientUpdateOpenMrsErrorLogControllerService.get(id)
    }

    @Transactional
    def save(PatientUpdateOpenMrsErrorLog patientUpdateOpenMrsErrorLog) {
        if (patientUpdateOpenMrsErrorLog == null) {
            render status: NOT_FOUND
            return
        }
        if (patientUpdateOpenMrsErrorLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientUpdateOpenMrsErrorLog.errors
            return
        }

        try {
            patientUpdateOpenMrsErrorLogControllerService.save(patientUpdateOpenMrsErrorLog)
        } catch (ValidationException e) {
            respond openmrsErrorLog.errors
            return
        }

        respond patientUpdateOpenMrsErrorLog, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PatientUpdateOpenMrsErrorLog patientUpdateOpenMrsErrorLog) {
        if (patientUpdateOpenMrsErrorLog == null) {
            render status: NOT_FOUND
            return
        }
        if (patientUpdateOpenMrsErrorLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientUpdateOpenMrsErrorLog.errors
            return
        }

        try {
            patientUpdateOpenMrsErrorLogControllerService.save(patientUpdateOpenMrsErrorLog)
        } catch (ValidationException e) {
            respond patientUpdateOpenMrsErrorLog.errors
            return
        }

        respond patientUpdateOpenMrsErrorLog, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientUpdateOpenMrsErrorLogControllerService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
