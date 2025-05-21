package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException

import static org.springframework.http.HttpStatus.*

@ReadOnly
class UiSectionController  extends RestfulController {

    UiSectionService uiSectionService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    UiSectionController() {
        super(UiSection)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond uiSectionService.list(params), model:[menuCount: uiSectionService.count()]
    }

    def show(Long id) {
        respond uiSectionService.get(id)
    }

    @Transactional
    def save(UiSection uiSection) {
        if (uiSection == null) {
            render status: NOT_FOUND
            return
        }
        if (uiSection.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond menu.errors
            return
        }

        try {
            uiSectionService.save(uiSection)
        } catch (ValidationException e) {
            respond uiSection.errors
            return
        }

        respond uiSection, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(UiSection uiSection) {
        if (uiSection == null) {
            render status: NOT_FOUND
            return
        }
        if (uiSection.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond uiSection.errors
            return
        }

        try {
            uiSectionService.save(uiSection)
        } catch (ValidationException e) {
            respond uiSection.errors
            return
        }

        respond uiSection, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || uiSectionService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
