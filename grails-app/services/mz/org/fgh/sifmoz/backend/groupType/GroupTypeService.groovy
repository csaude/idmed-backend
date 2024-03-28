package mz.org.fgh.sifmoz.backend.groupType

import grails.gorm.services.Service

@Service(GroupType)
interface GroupTypeService {

    GroupType get(Serializable id)

    List<GroupType> list(Map args)

    Long count()

    GroupType delete(Serializable id)

    GroupType save(GroupType groupType)

}
