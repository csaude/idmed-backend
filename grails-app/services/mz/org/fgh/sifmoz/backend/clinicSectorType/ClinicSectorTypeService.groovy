package mz.org.fgh.sifmoz.backend.clinicSectorType

import grails.gorm.services.Service

@Service(ClinicSectorType)
interface ClinicSectorTypeService {

    ClinicSectorType get(Serializable id)

    List<ClinicSectorType> list(Map args)

    Long count()

    ClinicSectorType delete(Serializable id)

    ClinicSectorType save(ClinicSectorType clinicSectorType)

}
