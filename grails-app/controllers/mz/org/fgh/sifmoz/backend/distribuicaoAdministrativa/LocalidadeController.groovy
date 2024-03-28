package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.converters.JSON
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class LocalidadeController {

    LocalidadeService localidadeService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(localidadeService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(localidadeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Localidade localidade = new Localidade()
        def objectJSON = request.JSON
        localidade = objectJSON as Localidade

        localidade.beforeInsert()
        localidade.validate()

        if(objectJSON.id){
            province.id = UUID.fromString(objectJSON.id)
        }

        if (localidade.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond localidade.errors
            return
        }

        try {
            localidadeService.save(localidade)
        } catch (ValidationException e) {
            respond localidade.errors
            return
        }

        respond localidade, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Localidade localidade) {
        if (localidade == null) {
            render status: NOT_FOUND
            return
        }
        if (localidade.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond localidade.errors
            return
        }

        try {
            localidadeService.save(localidade)
        } catch (ValidationException e) {
            respond localidade.errors
            return
        }

        respond localidade, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || localidadeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
