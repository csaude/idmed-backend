package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.gorm.services.Service

@Service(InventoryStockAdjustment)
interface InventoryStockAdjustmentService {

    InventoryStockAdjustment get(Serializable id)

    List<InventoryStockAdjustment> list(Map args)

    Long count()

    InventoryStockAdjustment delete(Serializable id)

    InventoryStockAdjustment save(InventoryStockAdjustment inventoryStockAdjustment)

}
