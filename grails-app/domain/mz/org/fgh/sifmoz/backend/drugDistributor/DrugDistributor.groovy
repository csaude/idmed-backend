package mz.org.fgh.sifmoz.backend.drugDistributor

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockDistributor.StockDistributor
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatch

class DrugDistributor extends BaseEntity {
    String id
    Drug drug
    Clinic clinic
    int quantity
    String status

    static belongsTo = [stockDistributor: StockDistributor]
    static hasMany = [stockDistributorBatchs: StockDistributorBatch]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Stock_Batch_Idx'
        datasource 'ALL'
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,dashboardMenuCode,stockMenuCode,homeMenuCode))
        }
        return menus
    }
}
