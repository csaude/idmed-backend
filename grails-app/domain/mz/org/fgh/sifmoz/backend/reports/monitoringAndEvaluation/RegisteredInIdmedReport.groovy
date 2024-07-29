package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

class RegisteredInIdmedReport extends BaseEntity{
    String id
    String reportId
    String pharmacyId
    String provinceId
    String districtId
    String period
    String periodType
    String month
    String quarter
    String semester
    int year
    Date startDate
    Date endDate


    String nid
    String firstName
    String lastName
    Date dateOfBirth
    String gender
    Date creationDate


    static constraints = {
        id generator: "uuid"
        pharmacyId nullable: true
        provinceId nullable: true
        districtId nullable: true
        period nullable: true
        month nullable: true
        quarter nullable: true
        semester nullable: true
        startDate nullable: true
        endDate nullable: true
        year nullable: true
        periodType nullable: true
        firstName nullable: true
        lastName nullable: true
        dateOfBirth nullable: true
        gender nullable: true
        creationDate nullable: true

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
