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
class MenuController {

    MenuService menuService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond menuService.list(params), model:[menuCount: menuService.count()]
    }

    def show(Long id) {
        respond menuService.get(id)
    }

    @Transactional
    def save(Menu menu) {
        if (menu == null) {
            render status: NOT_FOUND
            return
        }
        if (menu.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond menu.errors
            return
        }

        try {
            menuService.save(menu)
        } catch (ValidationException e) {
            respond menu.errors
            return
        }

        respond menu, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Menu menu) {
        if (menu == null) {
            render status: NOT_FOUND
            return
        }
        if (menu.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond menu.errors
            return
        }

        try {
            menuService.save(menu)
        } catch (ValidationException e) {
            respond menu.errors
            return
        }

        respond menu, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || menuService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
