package mz.org.fgh.sifmoz.backend.therapeuticRegimen

import grails.gorm.services.Service

@Service(TherapeuticRegimen)
interface TherapeuticRegimenService {

    TherapeuticRegimen get(Serializable id)

    List<TherapeuticRegimen> list(Map args)

    Long count()

    TherapeuticRegimen delete(Serializable id)

    TherapeuticRegimen save(TherapeuticRegimen therapeuticRegimen)

}
