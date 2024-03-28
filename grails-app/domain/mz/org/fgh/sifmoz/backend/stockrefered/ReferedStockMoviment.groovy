package mz.org.fgh.sifmoz.backend.stockrefered

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockadjustment.StockAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment

class ReferedStockMoviment extends BaseEntity {
    String id
    Date date
    String orderNumber
    String origin
    int quantity
    char updateStatus
    Clinic clinic
    static hasMany = [adjustments : StockReferenceAdjustment]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_ReferedStockMoviment_Idx'
    }

    static constraints = {
        date(nullable: false, blank: false )
        orderNumber(nullable: false, blank: false, maxSize: 100)
        origin(nullable: false, blank: false, maxSize: 100)
        quantity(min: 1)
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    @Override
    public String toString() {
        return "ReferedStockMoviment{" +
                "date=" + date +
                ", orderNumber='" + orderNumber + '\'' +
                ", origin='" + origin + '\'' +
                ", quantity=" + quantity +
                ", updateStatus=" + updateStatus +
                '}';
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
