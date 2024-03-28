package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.gorm.services.Service

@Service(Province)
interface ProvinceService {

    Province get(Serializable id)

    List<Province> list(Map args)

    Long count()

    Province delete(Serializable id)

    Province save(Province province)

    Province findProvinceByDescription(String description)

}
