package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.gorm.services.Service

@Service(Localidade)
interface LocalidadeService {

    Localidade get(Serializable id)

    List<Localidade> list(Map args)

    Long count()

    Localidade delete(Serializable id)

    Localidade save(Localidade localidade)

}
