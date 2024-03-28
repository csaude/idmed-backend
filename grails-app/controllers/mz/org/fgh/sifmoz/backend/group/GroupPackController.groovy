package mz.org.fgh.sifmoz.backend.group

import grails.rest.RestfulController
import grails.validation.ValidationException
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class GroupPackController extends RestfulController{

    GroupPackService groupPackService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    GroupPackController() {
        super(GroupPack)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond groupPackService.list(params), model:[groupPackCount: groupPackService.count()]
    }

    def show(String id) {
        respond groupPackService.get(id)
    }

    @Transactional
    def save() {

        GroupPack groupPack = new GroupPack()
        def objectJSON = request.JSON
        groupPack = objectJSON as GroupPack

        groupPack.beforeInsert()
        groupPack.validate()

        if(objectJSON.id){
            groupPack.id = UUID.fromString(objectJSON.id)
        }
        if (groupPack.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupPack.errors
            return
        }

        try {
            groupPackService.save(groupPack)
        } catch (ValidationException e) {
            respond groupPack.errors
            return
        }

        respond groupPack, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(GroupPack groupPack) {
        if (groupPack == null) {
            render status: NOT_FOUND
            return
        }
        if (groupPack.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupPack.errors
            return
        }

        try {
            groupPackService.save(groupPack)
        } catch (ValidationException e) {
            respond groupPack.errors
            return
        }

        respond groupPack, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || groupPackService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
