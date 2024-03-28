package mz.org.fgh.sifmoz.backend.interoperabilityType

import grails.gorm.services.Service

@Service(InteroperabilityType)
interface InteroperabilityTypeService {

    InteroperabilityType get(Serializable id)

    List<InteroperabilityType> list(Map args)

    Long count()

    InteroperabilityType delete(Serializable id)

    InteroperabilityType save(InteroperabilityType interoperabilityType)

}
