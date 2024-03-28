package mz.org.fgh.sifmoz.backend.episodeType

import grails.gorm.services.Service

@Service(EpisodeType)
interface EpisodeTypeService {

    EpisodeType get(Serializable id)

    List<EpisodeType> list(Map args)

    Long count()

    EpisodeType delete(Serializable id)

    EpisodeType save(EpisodeType episodeType)

}
