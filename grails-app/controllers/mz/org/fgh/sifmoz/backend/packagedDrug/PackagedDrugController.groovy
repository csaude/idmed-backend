package mz.org.fgh.sifmoz.backend.packagedDrug

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional


class PackagedDrugController extends RestfulController{

    IPackagedDrugService packagedDrugService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PackagedDrugController() {
        super(PackagedDrug)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 100, 100)
        render JSONSerializer.setObjectListJsonResponse(packagedDrugService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(packagedDrugService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PackagedDrug packagedDrug = new PackagedDrug()
        def objectJSON = request.JSON
        packagedDrug = objectJSON as PackagedDrug

        packagedDrug.beforeInsert()
        packagedDrug.validate()

        if(objectJSON.id){
            packagedDrug.id = UUID.fromString(objectJSON.id)
        }
        if (packagedDrug.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond packagedDrug.errors
            return
        }

        try {
            packagedDrugService.save(packagedDrug)
        } catch (ValidationException e) {
            respond packagedDrug.errors
            return
        }

        respond packagedDrug, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PackagedDrug packagedDrug) {
        if (packagedDrug == null) {
            render status: NOT_FOUND
            return
        }
        if (packagedDrug.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond packagedDrug.errors
            return
        }

        try {
            packagedDrugService.save(packagedDrug)
        } catch (ValidationException e) {
            respond packagedDrug.errors
            return
        }

        respond packagedDrug, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || packagedDrugService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByPackId(String packId) {
        respond packagedDrugService.getAllByPackId(packId)
    }
}
