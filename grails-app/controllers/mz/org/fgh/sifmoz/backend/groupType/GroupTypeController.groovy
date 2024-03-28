package mz.org.fgh.sifmoz.backend.groupType

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

class GroupTypeController extends RestfulController{

    GroupTypeService groupTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    GroupTypeController() {
        super(GroupType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(groupTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(groupTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {
        GroupType groupType = new GroupType()
        def objectJSON = request.JSON
        groupType = objectJSON as GroupType

        groupType.beforeInsert()
        groupType.validate()

        if(objectJSON.id){
            groupType.id = UUID.fromString(objectJSON.id)
        }
        if (groupType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupType.errors
            return
        }

        try {
            groupTypeService.save(groupType)
        } catch (ValidationException e) {
            respond groupType.errors
            return
        }

        respond groupType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(GroupType groupType) {
        if (groupType == null) {
            render status: NOT_FOUND
            return
        }
        if (groupType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupType.errors
            return
        }

        try {
            groupTypeService.save(groupType)
        } catch (ValidationException e) {
            respond groupType.errors
            return
        }

        respond groupType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || groupTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
