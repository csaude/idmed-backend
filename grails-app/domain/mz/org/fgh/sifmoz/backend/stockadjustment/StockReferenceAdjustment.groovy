package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockdestruction.DestroyedStock
import mz.org.fgh.sifmoz.backend.stockrefered.ReferedStockMoviment

class StockReferenceAdjustment extends StockAdjustment{
    String id
    static hasOne = [reference: ReferedStockMoviment]

    StockReferenceAdjustment() {
    }

    StockReferenceAdjustment(ReferedStockMoviment reference, Stock adjustedStock) {
        super(adjustedStock)
        this.reference = reference
    }

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_StockReferenceAdjustment_Idx'
    }

    static constraints = {
        reference nullable: false
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
            menus = Menu.findAllByCodeInList(Arrays.asList(dashboardMenuCode,stockMenuCode))
        }
        return menus
    }
}
