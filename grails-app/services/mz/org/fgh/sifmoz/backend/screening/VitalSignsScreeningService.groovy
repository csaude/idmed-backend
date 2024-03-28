package mz.org.fgh.sifmoz.backend.screening

import grails.gorm.services.Service

@Service(VitalSignsScreening)
interface VitalSignsScreeningService {

    VitalSignsScreening get(Serializable id)

    List<VitalSignsScreening> list(Map args)

    Long count()

    VitalSignsScreening delete(Serializable id)

    VitalSignsScreening save(VitalSignsScreening vitalSignsScreening)

}
