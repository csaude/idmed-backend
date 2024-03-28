package mz.org.fgh.sifmoz.backend.screening

import grails.gorm.services.Service

@Service(TBScreening)
interface TBScreeningService {

    TBScreening get(Serializable id)

    List<TBScreening> list(Map args)

    Long count()

    TBScreening delete(Serializable id)

    TBScreening save(TBScreening TBScreening)

}
