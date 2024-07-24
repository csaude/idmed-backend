package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class InventoryReportResponse  extends BaseEntity{
    String id
    String drugName
    List<InventoryReportTemp> adjustments
    Long totalAdjustedValue
    Long totalBalance

    static constraints = {
        id generator: "uuid"
    }

    static mapping = {
        datasource 'ALL'
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(reportsMenuCode))
        }
        return menus
    }

}
