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

class PackagedDrugStockController extends RestfulController{

    PackagedDrugStockService packagedDrugStockService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PackagedDrugStockController() {
        super(PackagedDrugStock)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(packagedDrugStockService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(packagedDrugStockService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PackagedDrugStock packagedDrugStock = new PackagedDrugStock()
        def objectJSON = request.JSON
        packagedDrugStock = objectJSON as PackagedDrugStock

        packagedDrugStock.beforeInsert()
        packagedDrugStock.validate()

        if(objectJSON.id){
            packagedDrugStock.id = UUID.fromString(objectJSON.id)
        }
        if (packagedDrugStock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond packagedDrugStock.errors
            return
        }

        try {
            packagedDrugStockService.save(packagedDrugStock)
        } catch (ValidationException e) {
            respond packagedDrugStock.errors
            return
        }

        respond packagedDrugStock, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(PackagedDrugStock packagedDrugStock) {
        if (packagedDrugStock == null) {
            render status: NOT_FOUND
            return
        }
        if (packagedDrugStock.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond packagedDrugStock.errors
            return
        }

        try {
            packagedDrugStockService.save(packagedDrugStock)
        } catch (ValidationException e) {
            respond packagedDrugStock.errors
            return
        }

        respond packagedDrugStock, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || packagedDrugStockService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}
