package mz.org.fgh.sifmoz.backend.serviceattribute

import grails.gorm.services.Service

@Service(ClinicalServiceAttribute)
interface ClinicalServiceAttributeService {

    ClinicalServiceAttribute get(Serializable id)

    List<ClinicalServiceAttribute> list(Map args)

    Long count()

    ClinicalServiceAttribute delete(Serializable id)

    ClinicalServiceAttribute save(ClinicalServiceAttribute clinicalServiceAttribute)
}
