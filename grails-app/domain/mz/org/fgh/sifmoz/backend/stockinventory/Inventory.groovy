package mz.org.fgh.sifmoz.backend.stockinventory

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment

class Inventory extends BaseEntity {
    String id
    Date startDate
    Date endDate
    boolean open
    boolean generic
    int sequence
    static hasMany = [adjustments : InventoryStockAdjustment]
    static transients = ['inventoryDrugs']
    List<Drug> inventoryDrugs
    Clinic clinic
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Inventory_Idx'
    }

    static constraints = {
        startDate(nullable: false, blank: false, validator: { startDate, urc ->
            return ((startDate <= new Date() ))
        })

        endDate(nullable: true, blank: true)
        adjustments( nulable: true)
    }

    public int  generateSequence(){
        return 0;
    }

    public void close() {
        this.open = false
        this.endDate = new Date()
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", open=" + open +
                ", sequence=" + sequence +
                '}';
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
            menus = Menu.findAllByCodeInList(Arrays.asList(stockMenuCode,homeMenuCode))
        }
        return menus
    }

}
