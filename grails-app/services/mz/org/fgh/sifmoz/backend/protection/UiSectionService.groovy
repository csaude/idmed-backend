package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service

@Service(UiSection)
interface UiSectionService {

    UiSection get(Serializable id)

    List<UiSection> list(Map args)

    Long count()

    UiSection delete(Serializable id)

    UiSection save(UiSection menu)

}
