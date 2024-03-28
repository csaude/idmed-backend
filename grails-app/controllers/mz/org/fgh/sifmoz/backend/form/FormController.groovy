package mz.org.fgh.sifmoz.backend.form

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional


class FormController extends RestfulController{

    FormService formService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    FormController() {
        super(Form)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(formService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(formService.get(id)) as JSON
    }

    @Transactional
    def save() {
        Form form = new Form()
        def objectJSON = request.JSON
        form = objectJSON as Form

        form.beforeInsert()
        form.validate()

        if(objectJSON.id){
            form.id = UUID.fromString(objectJSON.id)
        }
        if (form.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond form.errors
            return
        }

        try {
            formService.save(form)
        } catch (ValidationException e) {
            respond form.errors
            return
        }

        respond form, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Form form) {
        if (form == null) {
            render status: NOT_FOUND
            return
        }
        if (form.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond form.errors
            return
        }

        try {
            formService.save(form)
        } catch (ValidationException e) {
            respond form.errors
            return
        }

        respond form, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || formService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
