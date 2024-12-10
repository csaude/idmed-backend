package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockdestruction.DestroyedStock

class StockDestructionAdjustment extends StockAdjustment{
    String id
    static hasOne = [destruction: DestroyedStock]

    StockDestructionAdjustment() {
    }

    StockDestructionAdjustment(DestroyedStock destruction, Stock adjustedStock) {
        super(adjustedStock)
        this.destruction = destruction
    }

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_StockDestructionAdjustment_Idx'
        datasource 'ALL'
    }

    static constraints = {
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }


    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(dashboardMenuCode,stockMenuCode))
        }
        return menus
    }
}
