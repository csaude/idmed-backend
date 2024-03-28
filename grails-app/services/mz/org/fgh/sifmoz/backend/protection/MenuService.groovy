package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service

@Service(Menu)
interface MenuService {

    Menu get(Serializable id)

    List<Menu> list(Map args)

    Long count()

    Menu delete(Serializable id)

    Menu save(Menu menu)

}
