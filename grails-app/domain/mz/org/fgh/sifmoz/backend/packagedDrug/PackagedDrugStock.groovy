package mz.org.fgh.sifmoz.backend.packagedDrug

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock

class PackagedDrugStock extends BaseEntity {
    String id
    Drug drug
    double quantitySupplied
    Date creationDate = new Date()
    Stock stock
    static belongsTo = [packagedDrug: PackagedDrug]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PackagedDrugStock_Idx'
        datasource 'ALL'
    }

    static constraints = {
        creationDate nullable: true
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode,groupsMenuCode,stockMenuCode,dashboardMenuCode))
        }
        return menus
    }
}
