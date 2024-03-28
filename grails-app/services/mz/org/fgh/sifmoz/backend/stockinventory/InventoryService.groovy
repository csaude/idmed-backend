package mz.org.fgh.sifmoz.backend.stockinventory

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock

import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustmentService
import mz.org.fgh.sifmoz.backend.stockadjustment.StockAdjustment

@Transactional
@Service(Inventory)
abstract class InventoryService implements IInventoryService{
    IStockService stockService

    InventoryStockAdjustmentService adjustmentService

    @Override
    void processInventoryAdjustments(Inventory inventory) {

        for (StockAdjustment adjustment : inventory.getAdjustments()) {
            adjustment.setFinalised(true)
        }
    }

    @Override
    void initInventory(Inventory inventory) {
        List<Stock> drugStocks = new ArrayList<>()

        for (Drug drug : inventory.getInventoryDrugs()) {
            drugStocks.addAll(stockService.findAllOnceReceivedByDrug(drug))
        }

        initInventoryAdjustments(inventory, drugStocks)

    }

    private static void initInventoryAdjustments(Inventory inventory, List<Stock> stocks) {
        List<InventoryStockAdjustment> adjustments = new ArrayList<>()

        for(Stock stock : stocks){
            adjustments.add(initAdjustment(inventory, stock))
        }
        inventory.setAdjustments(adjustments as Set<InventoryStockAdjustment>)
    }

    private static InventoryStockAdjustment initAdjustment(Inventory inventory, Stock stock) {
        StockAdjustment adjustment = new InventoryStockAdjustment(inventory, stock)
        return adjustment
    }

    @Override
    List<Inventory> getAllByClinicId(String clinicId, int offset, int max) {
        return Inventory.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }
    @Override
    Inventory removeInventory(String id) {
        for (InventoryStockAdjustment adj :  InventoryStockAdjustment.findAllByInventory(Inventory.findById(id))) {
            adjustmentService.delete(adj.id)
        }
       return delete(id)

    }
    @Override
    boolean isInventoryPeriod(String clinicId) {
        List<Inventory> list = Inventory.executeQuery(" select i from Inventory i "+
                "where  i.clinic.id =: clinicId and i.open = false order by i.endDate desc " ,
                [clinicId: clinicId]);
        if (list.size() > 0) {
            Inventory inventory = list.first()
            Date otherDate = inventory.endDate
            long differenceInMilliseconds = new Date().time - otherDate.time
            long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000)
            return differenceInDays > 28
        } else {
            return true
        }

    }
}
