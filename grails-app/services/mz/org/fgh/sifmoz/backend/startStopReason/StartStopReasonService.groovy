package mz.org.fgh.sifmoz.backend.startStopReason

import grails.gorm.services.Service

@Service(StartStopReason)
interface StartStopReasonService {

    StartStopReason get(Serializable id)

    List<StartStopReason> list(Map args)

    Long count()

    StartStopReason delete(Serializable id)

    StartStopReason save(StartStopReason startStopReason)

}
