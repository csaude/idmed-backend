package mz.org.fgh.sifmoz.backend.stockDistributor

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stock.StockService
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatch
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatchService
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntranceService
import mz.org.fgh.sifmoz.backend.stockoperation.StockOperationType
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMoviment
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMovimentService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.*

class StockDistributorController extends RestfulController {

    IStockDistributorService stockDistributorService
    StockDistributorBatchService stockDistributorBatchService
    StockService stockService
    ReferedStockMovimentService referedStockMovimentService
    StockEntranceService stockEntranceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    StockDistributorController() {
        super(StockDistributor)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond stockDistributorService.list(params)
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(stockDistributorService.get(id)) as JSON
    }

    @Transactional
    def save() {
        StockDistributor stockDistributor = new StockDistributor()
        def objectJSON = request.JSON
        stockDistributor = objectJSON as StockDistributor

        stockDistributor.beforeInsert()
        stockDistributor.drugDistributors.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.drugDistributors[index].id)
        }
        stockDistributor.validate()

        if (objectJSON.id) {
            stockDistributor.id = UUID.fromString(objectJSON.id)
        }
        if (stockDistributor.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributor.errors
            return
        }
        try {
            stockDistributorService.save(stockDistributor)
        } catch (ValidationException e) {
            respond stockDistributor.errors
            return
        }

        respond stockDistributor, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        StockDistributor stockDistributoreDb = StockDistributor.get(objectJSON.id)
        if (stockDistributoreDb == null) {
            render status: NOT_FOUND
            return
        }
        stockDistributoreDb.properties = objectJSON
        stockDistributoreDb.drugDistributors.eachWithIndex { item, index ->
            item.id = UUID.fromString(objectJSON.drugDistributors[index].id)
        }
        if (stockDistributoreDb.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond stockDistributoreDb.errors
            return
        }

        try {
            stockDistributorService.save(stockDistributoreDb)
        } catch (ValidationException e) {
            respond stockDistributoreDb.errors
            return
        }

        respond stockDistributoreDb, [status: OK, view: "show"]
    }

    @Transactional
    def updateStockDistributorStatus(String idStockDistributor, String status) {
        StockDistributor stockDistributor = stockDistributorService.get(idStockDistributor)
        List<StockDistributorBatch> batchs = stockDistributorBatchService.getStockDistributorBatchByStockDistributorId(idStockDistributor)
        stockDistributor.setStatus(status)
        if (status.equalsIgnoreCase("C")) {
            List<Stock> newStockList = new ArrayList<>()
            StockEntrance entrance = getStockEntranceInstance(stockDistributor)
            for (StockDistributorBatch batch : batchs) {
                //make new Stock entrance because the batch number doesnt exist
                if (!stockService.existsBatchNumber(batch.getStock().getBatchNumber(), batch.getDrugDistributor().getClinicId())) {
                    generateNewStocksList(batch, newStockList, entrance)
                } else {
                    // make adjustments because the batch number exists
                    StockReferenceAdjustment reference = generateAdjustment(batch)
                    if (!Objects.isNull(reference)) {
                        referedStockMovimentService.save(reference)
                    }
                }
            }


            entrance.stocks = new ArrayList<Stock>()
            entrance.stocks.addAll(newStockList)
            if (!newStockList.isEmpty()) {
                stockEntranceService.save(entrance)
            }


        } else if (status.equalsIgnoreCase("R")) {
            // process Rejected Order
        }

        stockDistributorService.save(stockDistributor)

        respond stockDistributor, [status: OK, view: "show"]


    }

    private StockEntrance getStockEntranceInstance(StockDistributor stockDistributor) {
        StockEntrance entrance = new StockEntrance()
        entrance.setOrderNumber("Dist_" + stockDistributor.getOrderNumber())
        entrance.setClinic(stockDistributor.getDrugDistributors()[0].getClinic())
        entrance.setCreationDate(new Date())
        entrance.setDateReceived(new Date())
        entrance.setNotes("Entrada criada aparitir de distribuicao")
        entrance.id = UUID.randomUUID()
        return entrance
    }

    private ReferedStockMoviment generateAdjustment(StockDistributorBatch batch) {
        ReferedStockMoviment reference = new ReferedStockMoviment()
        reference.id = UUID.randomUUID()
        reference.setClinic(batch.getStockDistributor().getClinic())
        reference.setOrderNumber("Ordem_Ajuste_Distribuicao")
        reference.setOrigin("Ajuste_Distribuicao")
        reference.setDate(new Date())
        reference.setQuantity(batch.getQuantity())
        reference.updateStatus = 'P'

        StockReferenceAdjustment stockReferenceAdjustment = new StockReferenceAdjustment()
        stockReferenceAdjustment.adjustedValue = batch.getQuantity()
        stockReferenceAdjustment.setBalance(batch.getStock().stockMoviment + batch.getQuantity())
        stockReferenceAdjustment.id = UUID.randomUUID()
        stockReferenceAdjustment.setOperation(StockOperationType.findByCode("AJUSTE_POSETIVO"))
        stockReferenceAdjustment.setAdjustedStock(batch.getStock())

        stockReferenceAdjustment.setCaptureDate(batch.getStockDistributor().getCreationDate())
        stockReferenceAdjustment.setClinic(batch.getDrugDistributor().getClinic())
        stockReferenceAdjustment.setNotes("Recebimento de medicamentos vindo da farmacia para o sector")
        stockReferenceAdjustment.setReference(reference)

        reference.adjustments = new HashSet<StockReferenceAdjustment>()
        reference.adjustments.add(stockReferenceAdjustment)
        return reference
    }

    private void generateNewStocksList(StockDistributorBatch batch, ArrayList<Stock> newStockList, StockEntrance entrance) {
        Stock newStock = new Stock()
        newStock.id = UUID.randomUUID()
        newStock.setClinic(batch.getDrugDistributor().getClinic())
        newStock.setBatchNumber(batch.getStock().getBatchNumber())
        newStock.setCenter(batch.getStock().getCenter())
        newStock.setManufacture(batch.getStock().getManufacture())
        newStock.setExpireDate(batch.getStock().getExpireDate())
        newStock.setUnitsReceived(batch.getQuantity())
        newStock.setDrug(batch.getDrugDistributor().getDrug())
        newStock.setEntrance(entrance)
        newStockList.push(newStock)
    }

    @Transactional
    def delete(String id) {
        if (id == null || stockDistributorService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond stockDistributorService.getAllByClinicId(clinicId, offset, max)
    }
}
