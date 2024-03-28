package mz.org.fgh.sifmoz.backend.clinic

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.protection.Menu

@Service(Clinic)
interface ClinicService {

    Clinic get(Serializable id)

    List<Clinic> list(Map args)

    Long count()

    Clinic delete(Serializable id)

    Clinic save(Clinic clinic)

    Clinic findClinicByCode(String code)

}
