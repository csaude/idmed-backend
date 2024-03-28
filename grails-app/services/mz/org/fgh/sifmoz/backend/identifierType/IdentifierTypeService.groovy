package mz.org.fgh.sifmoz.backend.identifierType

import grails.gorm.services.Service

@Service(IdentifierType)
interface IdentifierTypeService {

    IdentifierType get(Serializable id)

    List<IdentifierType> list(Map args)

    Long count()

    IdentifierType delete(Serializable id)

    IdentifierType save(IdentifierType identifierType)

    IdentifierType getIdentifierTypeByCode(String code)

}
