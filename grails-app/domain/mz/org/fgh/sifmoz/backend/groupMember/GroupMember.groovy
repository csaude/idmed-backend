package mz.org.fgh.sifmoz.backend.groupMember

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.group.GroupInfo
import mz.org.fgh.sifmoz.backend.group.GroupMemberPrescription
import mz.org.fgh.sifmoz.backend.group.GroupPackHeader
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.protection.Menu

class GroupMember extends BaseEntity {
    String id
    Date startDate
    Date endDate
    // GroupInfo group
    Clinic clinic
    Patient patient

    static belongsTo = [group: GroupInfo]
    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_GroupMember_Idx'
        datasource 'ALL'
    }

    static constraints = {
        patient nullable: false, unique: ['group','endDate']
        startDate(nullable: false, blank: false, validator: { startDate, urc ->
            return startDate != null ? startDate <= new Date() : null
        })
        endDate nullable: true
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
            menus = Menu.findAllByCodeInList(Arrays.asList(groupsMenuCode))
        }
        return menus
    }
}
