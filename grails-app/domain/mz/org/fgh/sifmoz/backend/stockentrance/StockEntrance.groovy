package mz.org.fgh.sifmoz.backend.stockentrance

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock

class StockEntrance extends BaseEntity {
    String id
    String orderNumber
    Date dateReceived
    Clinic clinic
    Date creationDate
    String notes
    boolean isDistribution
    static hasMany = [stocks: Stock]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_StockEntrance_Idx'
    }

    static constraints = {
        orderNumber(nullable: false, blank: false, unique: ['dateReceived','clinic'] )
        dateReceived(nullable: false, blank: false, validator: { dateReceived, urc ->
            return ((dateReceived <= new Date()))
        })
        creationDate nullable: true
        notes nullable: true
    }

    @Override
    public String toString() {
        return "StockEntrance{" +
                "stocks=" + stocks +
                ", id='" + id + '\'' +
                ", orderNumber='" + orderNumber + '\'' +
                ", dateReceived=" + dateReceived +
                ", clinic=" + clinic +
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
