package mz.org.fgh.sifmoz.backend.stockdestruction

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment

class DestroyedStock extends BaseEntity {
    String id
    String notes
    String updateStatus
    Date date
    Clinic clinic
    static hasMany = [adjustments : StockDestructionAdjustment]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_DestroyedStock_Idx'
    }

    static constraints = {
        notes(nullable: false, blank: false)
    }

    @Override
    public String toString() {
        return "DestroyedStock{" +
                "notes='" + notes + '\'' +
                ", updateStatus='" + updateStatus + '\'' +
                '}';
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
            menus = Menu.findAllByCodeInList(Arrays.asList(stockMenuCode,homeMenuCode))
        }
        return menus
    }

}
