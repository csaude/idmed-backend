package mz.org.fgh.sifmoz.backend.groupMember

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.group.GroupInfo
import mz.org.fgh.sifmoz.group.GroupMemberInfoView


interface IGroupMemberService {

    GroupMember get(Serializable id)

    List<GroupMember> list(Map args)

    Long count()

    GroupMember delete(Serializable id)

    GroupMember save(GroupMember groupMember)

    List<GroupMemberInfoView> getMembersInfoByGroupId(String groupId)

}
