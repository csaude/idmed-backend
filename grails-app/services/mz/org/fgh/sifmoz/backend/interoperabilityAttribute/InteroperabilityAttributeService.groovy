package mz.org.fgh.sifmoz.backend.interoperabilityAttribute

import grails.gorm.services.Service

@Service(InteroperabilityAttribute)
interface InteroperabilityAttributeService {

    InteroperabilityAttribute get(Serializable id)

    List<InteroperabilityAttribute> list(Map args)

    Long count()

    InteroperabilityAttribute delete(Serializable id)

    InteroperabilityAttribute save(InteroperabilityAttribute interoperabilityAttribute)

}
