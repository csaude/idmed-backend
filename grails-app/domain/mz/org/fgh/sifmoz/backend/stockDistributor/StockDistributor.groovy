package mz.org.fgh.sifmoz.backend.stockDistributor

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drugDistributor.DrugDistributor
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockDistributorBatch.StockDistributorBatch

class StockDistributor extends BaseEntity {
    String id
    String orderNumber
    Date creationDate = new Date()
    String notes
    Clinic clinic


    static hasMany = [drugDistributors: DrugDistributor]



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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode, groupsMenuCode, dashboardMenuCode, stockMenuCode, homeMenuCode))
        }
        return menus
    }
}
