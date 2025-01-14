package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.linhasUsadas

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu

class LinhasUsadasReport extends BaseEntity{

    String id
    String reportId
    String clinicId
    String periodType
    int period
    int year
    Date startDate
    Date endDate

    String codigoRegime
    String regimeTerapeutico
    String linhaTerapeutica
    String estado
    int totalPrescricoes

    static belongsTo = Clinic

    static mapping = {
        id generator: "uuid"
    }

    static constraints = {
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
