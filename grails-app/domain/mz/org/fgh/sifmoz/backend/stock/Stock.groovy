package mz.org.fgh.sifmoz.backend.stock

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stockadjustment.StockAdjustment
import mz.org.fgh.sifmoz.backend.stockcenter.StockCenter
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance

class Stock extends BaseEntity {
    String id
    Date expireDate
    boolean modified
    String shelfNumber
    int unitsReceived
    int stockMoviment
    String manufacture
    String batchNumber
    boolean hasUnitsRemaining
    Drug drug
    StockCenter center
    Clinic clinic
    static hasMany = [packagedDrugs: PackagedDrugStock, adjustments: StockAdjustment]
    static belongsTo = [entrance: StockEntrance]


    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_Stock_Idx'
        datasource 'ALL'
    }

    static constraints = {
        expireDate(nullable: false, blank: false)
        batchNumber(nullable: false, blank: false)
        shelfNumber(nullable: true)
        unitsReceived(min: 0)
        manufacture(nullable: true)
        packagedDrugs nullable: true
        adjustments nullable: true
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
