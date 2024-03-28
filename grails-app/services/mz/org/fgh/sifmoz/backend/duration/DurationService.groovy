package mz.org.fgh.sifmoz.backend.duration

import grails.gorm.services.Service

@Service(Duration)
interface DurationService {

    Duration get(Serializable id)

    List<Duration> list(Map args)

    Long count()

    Duration delete(Serializable id)

    Duration save(Duration episode)
}
