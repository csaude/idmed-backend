package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.drugDistributor.DrugDistributor
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockDistributor.StockDistributor

class StockDistributorBatch extends BaseEntity {
    String id
    DrugDistributor drugDistributor
    int quantity
    Stock stock
    StockDistributor stockDistributor

    static belongsTo = [drugDistributor: DrugDistributor, stockDistributor : StockDistributor, stock: Stock]


    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Stock_Batch_Idx'
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode, groupsMenuCode, dashboardMenuCode, stockMenuCode, homeMenuCode))
        }
        return menus
    }
}
