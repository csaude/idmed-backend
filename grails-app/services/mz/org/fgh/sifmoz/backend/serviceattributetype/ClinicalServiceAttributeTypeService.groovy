package mz.org.fgh.sifmoz.backend.serviceattributetype

import grails.gorm.services.Service

@Service(ClinicalServiceAttributeType)
interface ClinicalServiceAttributeTypeService {

    ClinicalServiceAttributeType get(Serializable id)

    List<ClinicalServiceAttributeType> list(Map args)

    Long count()

    ClinicalServiceAttributeType delete(Serializable id)

    ClinicalServiceAttributeType save(ClinicalServiceAttributeType clinicalServiceAttributeType)
}
