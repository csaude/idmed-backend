package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service

@Service(SecUserRole)
interface SecUserRoleService {

    SecUserRole get(Serializable id)

    List<SecUserRole> list(Map args)

    Long count()

    SecUserRole delete(Serializable id)

    SecUserRole save(SecUserRole secUserRole)

}
