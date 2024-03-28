package mz.org.fgh.sifmoz.backend.group

import grails.rest.Resource
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.groupMember.GroupMember
import mz.org.fgh.sifmoz.backend.groupType.GroupType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.protection.Menu
import mz.org.fgh.sifmoz.backend.service.ClinicalService

class GroupInfo extends BaseEntity {
    String id
    String code
    String name
    Date startDate
    Date endDate
    Clinic clinic
    GroupType groupType
    ClinicalService service
    DispenseType dispenseType
    Date creationDate = new Date()

    static hasMany = [members: GroupMember, packHeaders: GroupPackHeader]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_GroupInfo_Idx'
        table 'group_info'
    }

    static constraints = {
        code nullable: false, unique: true
        name nullable: false
        startDate(nullable: false, blank: false, validator: { startDate, urc ->
            return startDate != null ? startDate <= new Date() : null
        })
        endDate(nullable: true, blank: true)
        packHeaders nullable: true
        creationDate nullable: true
        dispenseType nullable : true
    }

    public boolean isActive() {
        return this.endDate == null
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
        if (!clinic) {
            clinic = Clinic.findByMainClinic(true)
        }
    }


    @Override
    public String toString() {
        return "GroupInfo{" +
                "id='" + id + '\'' +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", clinic=" + clinic +
                ", groupType=" + groupType +
                ", dispenseType=" + dispenseType +
                ", service=" + service +
                '}';
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(groupsMenuCode))
        }
        return menus
    }
}
