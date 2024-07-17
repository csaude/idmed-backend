package mz.org.fgh.sifmoz.backend.packagedDrug

import com.fasterxml.jackson.annotation.JsonBackReference
import com.fasterxml.jackson.annotation.JsonManagedReference
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.stock.Stock

class PackagedDrug extends BaseEntity {
    String id
    @JsonManagedReference
    Drug drug
    double amtPerTime
    int timesPerDay
    String form
    double quantitySupplied
    Date nextPickUpDate
    boolean toContinue
    Date creationDate = new Date()
    Pack pack
    int quantityRemain
    Clinic clinic
    static belongsTo = [Pack]

    static hasMany = [packagedDrugStocks: PackagedDrugStock]

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_PackagedDrug_Idx'

    }

    static constraints = {
        amtPerTime nullable: true
        timesPerDay nullable: true
        form nullable: true
        nextPickUpDate nullable: true
        creationDate nullable: true
        clinic nullable: false
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
            menus = Menu.findAllByCodeInList(Arrays.asList(patientMenuCode, groupsMenuCode, stockMenuCode, dashboardMenuCode))
        }
        return menus
    }
}
