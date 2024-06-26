package mz.org.fgh.sifmoz.backend.stockadjustment


import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory

class InventoryStockAdjustment extends StockAdjustment{
    String id
    static hasOne = [inventory: Inventory]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_InventoryStockAdjustment_Idx'
        inventory updateable: false
    }

    InventoryStockAdjustment() {
    }

    InventoryStockAdjustment(Inventory inventory, Stock adjustedStock) {
        super(adjustedStock)
        this.inventory = inventory
    }

    static constraints = {
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
    }


    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(dashboardMenuCode,stockMenuCode,homeMenuCode))
        }
        return menus
    }
}
