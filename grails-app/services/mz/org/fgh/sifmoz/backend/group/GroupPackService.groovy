package mz.org.fgh.sifmoz.backend.group

import grails.gorm.services.Service

@Service(GroupPack)
interface GroupPackService {

    GroupPack get(Serializable id)

    List<GroupPack> list(Map args)

    Long count()

    GroupPack delete(Serializable id)

    GroupPack save(GroupPack group)
}
