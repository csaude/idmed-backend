package mz.org.fgh.sifmoz.backend.nationalClinic

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.protection.Menu

class NationalClinic extends BaseEntity {
    String id
    String code
    String facilityName
    String telephone
    FacilityType facilityType
    boolean active

    static belongsTo = [province: Province]
    static hasMany = [clinics: Clinic]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_NationalClinic_Idx'
    }

    static constraints = {
        code nullable: false, unique: true
        facilityName nullable: false, blank: false, unique: ['province']
        telephone nullable: true, matches: /\d+/, maxSize: 12, minSize: 9
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
            menus = Menu.findAllByCodeInList(Arrays.asList(administrationMenuCode))
        }
        return menus
    }
}
