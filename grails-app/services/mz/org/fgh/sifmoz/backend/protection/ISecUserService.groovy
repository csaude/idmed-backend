package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service


interface ISecUserService {

    SecUser get(Serializable id)

    List<SecUser> list(Map args)

    Long count()

    SecUser delete(Serializable id)

    SecUser save(SecUser secUser)

    SecUser saveSecUserAndRoles (SecUser secUser,List<Role> roles)

}
