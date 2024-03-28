package mz.org.fgh.sifmoz.backend.screening

import grails.gorm.services.Service

@Service(RAMScreening)
interface RAMScreeningService {

    RAMScreening get(Serializable id)

    List<RAMScreening> list(Map args)

    Long count()

    RAMScreening delete(Serializable id)

    RAMScreening save(RAMScreening RAMScreening)

}
