package mz.org.fgh.sifmoz.backend.episodeType

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

class EpisodeTypeController extends RestfulController{

    EpisodeTypeService episodeTypeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    EpisodeTypeController() {
        super(EpisodeType)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(episodeTypeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(episodeTypeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        EpisodeType episodeType = new EpisodeType()
        def objectJSON = request.JSON
        episodeType = objectJSON as EpisodeType

        episodeType.beforeInsert()
        episodeType.validate()

        if(objectJSON.id){
            episodeType.id = UUID.fromString(objectJSON.id)
        }

        if (episodeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond episodeType.errors
            return
        }

        try {
            episodeTypeService.save(episodeType)
        } catch (ValidationException e) {
            respond episodeType.errors
            return
        }

        respond episodeType, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(EpisodeType episodeType) {
        if (episodeType == null) {
            render status: NOT_FOUND
            return
        }
        if (episodeType.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond episodeType.errors
            return
        }

        try {
            episodeTypeService.save(episodeType)
        } catch (ValidationException e) {
            respond episodeType.errors
            return
        }

        respond episodeType, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || episodeTypeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
