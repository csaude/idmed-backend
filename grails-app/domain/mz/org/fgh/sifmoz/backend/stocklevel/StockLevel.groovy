package mz.org.fgh.sifmoz.backend.stocklevel

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock

class StockLevel extends BaseEntity {
    String id
    Clinic clinic
    Drug drug
    int quantity

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_StockLevel_Idx'
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
            menus = Menu.findAllByCodeInList(Arrays.asList(stockMenuCode))
        }
        return menus
    }

    @Override
    public String toString() {
        return "StockLevel{" +
                "id='" + id + '\'' +
                ", clinic=" + clinic +
                ", drug=" + drug +
                ", quantity=" + quantity +
                '}';
    }
}
