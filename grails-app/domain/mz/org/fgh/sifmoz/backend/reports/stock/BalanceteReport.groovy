package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.protection.Menu

class BalanceteReport extends BaseEntity{

    String id
    String reportId
    String clinicId
    String periodType
    int period
    int year
    Date startDate
    Date endDate

    String fnm
    Date diaDoEvento
    String medicamento
    String unidade
    int entradas
    int perdasEAjustes
    int saidas
    int stockExistente
    String notas
    Date validadeMedicamento

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
