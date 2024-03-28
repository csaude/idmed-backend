package mz.org.fgh.sifmoz.backend.screening

import grails.gorm.services.Service

@Service(PregnancyScreening)
interface PregnancyScreeningService {

    PregnancyScreening get(Serializable id)

    List<PregnancyScreening> list(Map args)

    Long count()

    PregnancyScreening delete(Serializable id)

    PregnancyScreening save(PregnancyScreening pregnancyScreening)

}
