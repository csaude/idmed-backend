package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service


@Service(Requestmap)
interface RequestmapService {

    Requestmap get(Serializable id)

    List<Requestmap> list(Map args)

    Long count()

    Requestmap delete(Serializable id)

    Requestmap save(Requestmap requestmap)

}
