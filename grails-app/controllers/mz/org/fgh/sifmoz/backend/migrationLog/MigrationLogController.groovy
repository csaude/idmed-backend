package mz.org.fgh.sifmoz.backend.migrationLog

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class MigrationLogController {

    MigrationLogService migrationLogService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond migrationLogService.list(params), model:[migrationLogCount: migrationLogService.count()]
    }

    def show(Long id) {
        respond migrationLogService.get(id)
    }

    @Transactional
    def save(MigrationLog migrationLog) {
        if (migrationLog == null) {
            render status: NOT_FOUND
            return
        }
        if (migrationLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond migrationLog.errors
            return
        }

        try {
            migrationLogService.save(migrationLog)
        } catch (ValidationException e) {
            respond migrationLog.errors
            return
        }

        respond migrationLog, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(MigrationLog migrationLog) {
        if (migrationLog == null) {
            render status: NOT_FOUND
            return
        }
        if (migrationLog.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond migrationLog.errors
            return
        }

        try {
            migrationLogService.save(migrationLog)
        } catch (ValidationException e) {
            respond migrationLog.errors
            return
        }

        respond migrationLog, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || migrationLogService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
