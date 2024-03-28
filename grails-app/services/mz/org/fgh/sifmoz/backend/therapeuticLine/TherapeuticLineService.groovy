package mz.org.fgh.sifmoz.backend.therapeuticLine

import grails.gorm.services.Service

@Service(TherapeuticLine)
interface TherapeuticLineService {

    TherapeuticLine get(Serializable id)

    List<TherapeuticLine> list(Map args)

    Long count()

    TherapeuticLine delete(Serializable id)

    TherapeuticLine save(TherapeuticLine therapeuticLine)

}
