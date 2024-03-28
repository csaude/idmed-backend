package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.converters.JSON
import grails.rest.RestfulController
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.grails.datastore.mapping.validation.ValidationException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class InventoryStockAdjustmentController extends RestfulController{

    InventoryStockAdjustmentService inventoryStockAdjustmentService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    InventoryStockAdjustmentController() {
        super(InventoryStockAdjustment)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(inventoryStockAdjustmentService.list(params)) as JSON
    }

    def show(String id) {
        def inventorySAdjustment =  inventoryStockAdjustmentService.get(id)
        if (inventorySAdjustment == null) {
            render status: NO_CONTENT
        } else {
            render JSONSerializer.setJsonObjectResponse(inventorySAdjustment) as JSON
        }
    }

    @Transactional
    def save() {

        InventoryStockAdjustment inventoryStockAdjustment = new InventoryStockAdjustment()
        def objectJSON = request.JSON
        inventoryStockAdjustment = objectJSON as InventoryStockAdjustment

        inventoryStockAdjustment.beforeInsert()
        inventoryStockAdjustment.validate()

        if(objectJSON.id){
            inventoryStockAdjustment.id = UUID.fromString(objectJSON.id)
        }

        if (inventoryStockAdjustment.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inventoryStockAdjustment.errors
            return
        }

        try {
            inventoryStockAdjustmentService.save(inventoryStockAdjustment)
        } catch (ValidationException e) {
            respond inventoryStockAdjustment.errors
            return
        }

        respond inventoryStockAdjustment, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {


        def objectJSON = request.JSON
        InventoryStockAdjustment inventoryStockAdjustmentDB = InventoryStockAdjustment.get(objectJSON.id)
        if (inventoryStockAdjustmentDB == null) {
            render status: NOT_FOUND
            return
        }
        //updating db object
        inventoryStockAdjustmentDB.properties =  objectJSON as InventoryStockAdjustment

        if (inventoryStockAdjustmentDB.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond inventoryStockAdjustmentDB.errors
            return
        }

        try {
            inventoryStockAdjustmentService.save(inventoryStockAdjustmentDB)
        } catch (grails.validation.ValidationException e) {
            respond inventoryStockAdjustmentDB.errors
            return
        }

        respond inventoryStockAdjustmentDB, [status: OK, view:"show"]

    }


    @Transactional
    def delete(String id) {
        if (id == null || inventoryStockAdjustmentService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}