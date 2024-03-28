package mz.org.fgh.sifmoz.backend.dispenseType

import grails.gorm.services.Service

@Service(DispenseType)
interface DispenseTypeService {

    DispenseType get(Serializable id)

    List<DispenseType> list(Map args)

    Long count()

    DispenseType delete(Serializable id)

    DispenseType save(DispenseType dispenseType)

}
