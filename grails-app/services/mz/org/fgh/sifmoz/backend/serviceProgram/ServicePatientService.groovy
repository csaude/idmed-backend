package mz.org.fgh.sifmoz.backend.serviceProgram

import grails.gorm.services.Service

@Service(ServicePatient)
interface ServicePatientService {

    ServicePatient get(Serializable id)

    List<ServicePatient> list(Map args)

    Long count()

    ServicePatient delete(Serializable id)

    ServicePatient save(ServicePatient servicePatient)

}
