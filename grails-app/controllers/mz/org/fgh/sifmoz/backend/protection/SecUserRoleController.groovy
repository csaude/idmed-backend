package mz.org.fgh.sifmoz.backend.protection

import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class SecUserRoleController {

    SecUserRoleService secUserRoleService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond secUserRoleService.list(params), model:[secUserRoleCount: secUserRoleService.count()]
    }

    def show(Long id) {
        respond secUserRoleService.get(id)
    }

    @Transactional
    def save(SecUserRole secUserRole) {
        if (secUserRole == null) {
            render status: NOT_FOUND
            return
        }
        if (secUserRole.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUserRole.errors
            return
        }

        try {
           // secUserRoleService.save(secUserRole)
            SecUserRole.create(secUserRole.getSecUser(), secUserRole.getRole())
        } catch (ValidationException e) {
            respond secUserRole.errors
            return
        }

        respond secUserRole, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(SecUserRole secUserRole) {
        if (secUserRole == null) {
            render status: NOT_FOUND
            return
        }
        if (secUserRole.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUserRole.errors
            return
        }

        try {
            secUserRoleService.save(secUserRole)
        } catch (ValidationException e) {
            respond secUserRole.errors
            return
        }

        respond secUserRole, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || secUserRoleService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
