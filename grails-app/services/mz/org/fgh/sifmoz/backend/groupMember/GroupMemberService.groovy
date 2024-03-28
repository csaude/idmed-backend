package mz.org.fgh.sifmoz.backend.groupMember

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import mz.org.fgh.sifmoz.group.GroupMemberInfoView
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Service(GroupMember)
abstract class GroupMemberService implements IGroupMemberService {
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Autowired
    SessionFactory sessionFactory


    def List<GroupMemberInfoView> getMembersInfoByGroupId(String groupId) {
        List<GroupMemberInfoView> getMembersInfoArrayList = new ArrayList<>()
        Session session = sessionFactory.getCurrentSession()

        String queryString = "select * " +
                "from patient_info_group_view pigw " +
                "where group_id = :groupId "


        def query = session.createSQLQuery(queryString)
        query.setParameter("groupId", groupId)
        List<Object[]> result = query.list()

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            addGroupMemberInfo(result, getMembersInfoArrayList)
        }

        getMembersInfoArrayList.sort { a, b ->
            def membershipEndDateA = a.membershipEndDate ?: new Date(Long.MIN_VALUE)
            def membershipEndDateB = b.membershipEndDate ?: new Date(Long.MIN_VALUE)
            membershipEndDateA <=> membershipEndDateB
        }
        return getMembersInfoArrayList
    }

    private void addGroupMemberInfo(List result, ArrayList<GroupMemberInfoView> getMembersInfoArrayList) {
        for (int i = 0; i < result.size(); i++) {

            GroupMemberInfoView groupMemberInfoView = new GroupMemberInfoView()
            Object rowView = result[i]
            groupMemberInfoView.fullName = String.valueOf(rowView[0])
            groupMemberInfoView.NID = String.valueOf(String.valueOf(rowView[1]))
            groupMemberInfoView.lastPrescriptionDate = rowView[2] != null ? formatter.parse(rowView[2].toString()) : null
            groupMemberInfoView.lastPickupDate = rowView[3] != null ? formatter.parse(rowView[3].toString()) : null
            groupMemberInfoView.nextPickupDate =  rowView[4] != null ? formatter.parse(rowView[4].toString()) : null
            groupMemberInfoView.validade = Long.valueOf(rowView[5])
            groupMemberInfoView.lastPrescriptionDateMember = rowView[6] != null ? formatter.parse(rowView[6].toString()) : null
            groupMemberInfoView.validadeNova = Long.valueOf(rowView[7])
            groupMemberInfoView.patientId =  String.valueOf(rowView[8])
            groupMemberInfoView.groupMemberId = String.valueOf(rowView[9])
            groupMemberInfoView.patientServiceId = String.valueOf(rowView[10])
            groupMemberInfoView.episodeId = String.valueOf(rowView[11])
            groupMemberInfoView.membershipEndDate = rowView[12] != null ? formatter.parse(rowView[12].toString()) : null
            groupMemberInfoView.groupId = String.valueOf(rowView[13])

          println(groupMemberInfoView)
            getMembersInfoArrayList.add(groupMemberInfoView)
        }
    }
}
