package mz.org.fgh.sifmoz.backend.screening

import grails.gorm.services.Service

@Service(AdherenceScreening)
interface AdherenceScreeningService {

    AdherenceScreening get(Serializable id)

    List<AdherenceScreening> list(Map args)

    Long count()

    AdherenceScreening delete(Serializable id)

    AdherenceScreening save(AdherenceScreening adherenceScreening)

}
