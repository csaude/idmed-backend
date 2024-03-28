package mz.org.fgh.sifmoz.backend.nationalClinic

import grails.gorm.services.Service

@Service(NationalClinic)
interface NationalClinicService {

    NationalClinic get(Serializable id)

    List<NationalClinic> list(Map args)

    Long count()

    NationalClinic delete(Serializable id)

    NationalClinic save(NationalClinic nationalClinic)

}
