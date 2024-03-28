package mz.org.fgh.sifmoz.backend.service

import grails.gorm.services.Service

@Service(ClinicalService)
interface ClinicalServiceService {

    ClinicalService get(Serializable id)

    List<ClinicalService> list(Map args)

    Long count()

    ClinicalService delete(Serializable id)

    ClinicalService save(ClinicalService clinicalService)
}
