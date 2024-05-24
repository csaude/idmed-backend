package mz.org.fgh.sifmoz.backend.drugDistributor

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatch
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatchService
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustmentService
import mz.org.fgh.sifmoz.backend.stockoperation.StockOperationType
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMoviment
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMovimentService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.*

class DrugDistributorController extends RestfulController {

    StockDistributorBatchService stockDistributorBatchService
    StockReferenceAdjustmentService stockReferenceAdjustmentService
    ReferedStockMovimentService referedStockMovimentService
    DrugDistributorService drugDistributorService

    IStockService stockService
    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    DrugDistributorController() {
        super(Stock)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond drugDistributorService.list(params)
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(drugDistributorService.get(id)) as JSON
    }

    @Transactional
    def save() {
        DrugDistributor drugDistributor = new DrugDistributor()
        def objectJSON = request.JSON
        drugDistributor = objectJSON as DrugDistributor

        drugDistributor.beforeInsert()
        drugDistributor.validate()

        if (objectJSON.id) {
            drugDistributor.id = UUID.fromString(objectJSON.id)
        }
        if (drugDistributor.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond drugDistributor.errors
            return
        }

        try {
            makeDistribution(drugDistributor)


        } catch (ValidationException e) {
            respond drugDistributor.errors
            return
        }

        respond drugDistributor, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON

        DrugDistributor drugDistributor = DrugDistributor.findWhere(id: objectJSON.id)

        if (drugDistributor == null) {
            render status: NOT_FOUND
            return
        }
        if (drugDistributor.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond drugDistributor.errors
            return
        }

        try {


            drugDistributor.quantity = Integer.parseInt(objectJSON.getAt("quantity").toString())
            //stock.stockMoviment = Integer.parseInt(objectJSON.getAt("stockMoviment").toString())
            //stock.manufacture = objectJSON.getAt("manufacture").toString()
            // stock.batchNumber = objectJSON.getAt("batchNumber").toString()
            drugDistributor.drug = Drug.findWhere(id: objectJSON?.getAt("drug")?.getAt("id")?.toString())
            drugDistributor.clinic = Clinic.findWhere(id: objectJSON?.getAt("clinic")?.getAt("id")?.toString())

            stockDistributorBatchService.save(drugDistributor)
        } catch (ValidationException e) {
            respond drugDistributor.errors
            return
        }

        respond drugDistributor, [status: OK, view: "show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || drugDistributorService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }


    void makeDistribution(DrugDistributor drugDistributor) {

        drugDistributorService.save(drugDistributor)

        int quantityAux = drugDistributor.quantity
        List<Stock> stocks = stockService.getValidStockByDrug(drugDistributor.getDrug())
        if (stocks.size() == 0) render status: NO_CONTENT

        ReferedStockMoviment reference = new ReferedStockMoviment()
        reference.id = UUID.randomUUID()
        reference.setClinic(drugDistributor.getStockDistributor().getClinic())
        reference.setOrderNumber("Ordem_Ajuste_Distribuicao")
        reference.setOrigin("Ajuste_Distribuicao")
        reference.setDate(new Date())
        reference.setQuantity(quantityAux)
        reference.updateStatus = 'P'

        // Validate if there is stock enough
        for (Stock stock : stocks) {

            StockReferenceAdjustment stockReferenceAdjustment = new StockReferenceAdjustment()
            StockDistributorBatch stockDistributorBatch = new StockDistributorBatch()
            stockDistributorBatch.id = UUID.randomUUID()
            stockDistributorBatch.setStock(stock)
            stockDistributorBatch.setDrugDistributor(drugDistributor)
            stockDistributorBatch.setStockDistributor(drugDistributor.getStockDistributor())
            if (quantityAux != 0) {
                if (stock.stockMoviment >= quantityAux) {
                    //Create Negative adjustment
                    stock.stockMoviment = stock.stockMoviment - quantityAux
                    stockReferenceAdjustment.adjustedValue = quantityAux
                    stockReferenceAdjustment.setBalance(stock.stockMoviment)
                    stockDistributorBatch.setQuantity(quantityAux)
                    quantityAux = 0

                } else {
                    quantityAux = quantityAux - stock.stockMoviment
                    stockReferenceAdjustment.adjustedValue = stock.stockMoviment
                    stockReferenceAdjustment.setBalance(stock.stockMoviment)
                    stockDistributorBatch.setQuantity(stock.stockMoviment)
                    stock.stockMoviment = 0
                }
                stockService.save(stock)
                stockDistributorBatchService.save(stockDistributorBatch)

                stockReferenceAdjustment.id = UUID.randomUUID()
                stockReferenceAdjustment.setOperation(StockOperationType.findByCode("AJUSTE_NEGATIVO"))
                stockReferenceAdjustment.setAdjustedStock(stock)

                stockReferenceAdjustment.setCaptureDate(drugDistributor.getStockDistributor().getCreationDate())
                stockReferenceAdjustment.setClinic(drugDistributor.getStockDistributor().getClinic())
                stockReferenceAdjustment.setNotes("Distribuicao de medicamentos para os sectores clinicos")
                stockReferenceAdjustment.setReference(reference)
                reference.adjustments = new HashSet<StockReferenceAdjustment>()
                reference.adjustments.add(stockReferenceAdjustment)
                //  stockReferenceAdjustmentService.save(stockReferenceAdjustment)
                referedStockMovimentService.save(reference)

            } else {
                break;
            }
        }
    }


}
