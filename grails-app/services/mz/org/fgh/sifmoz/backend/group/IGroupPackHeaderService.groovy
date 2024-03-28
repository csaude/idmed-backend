package mz.org.fgh.sifmoz.backend.group

import grails.gorm.services.Service

interface IGroupPackHeaderService {

    GroupPackHeader get(Serializable id)

    List<GroupPackHeader> list(Map args)

    Long count()

    GroupPackHeader delete(Serializable id)

    GroupPackHeader save(GroupPackHeader group)
}
