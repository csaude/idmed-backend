package mz.org.fgh.sifmoz.backend.group

import grails.gorm.services.Service

@Service(GroupMemberPrescription)
interface GroupPrescriptionService {

    GroupMemberPrescription get(Serializable id)

    List<GroupMemberPrescription> list(Map args)

    Long count()

    GroupMemberPrescription delete(Serializable id)

    GroupMemberPrescription save(GroupMemberPrescription groupPrescription)

}
