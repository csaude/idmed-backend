package mz.org.fgh.sifmoz.backend.group

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.groupMember.GroupMember
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.protection.Menu

class GroupMemberPrescription extends BaseEntity{

    String id
    Prescription prescription
    GroupMember member
    boolean used

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_GroupMemberPrescription_Idx'
        datasource 'ALL'
    }

    static constraints = {
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
