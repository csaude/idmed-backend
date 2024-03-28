package mz.org.fgh.sifmoz.backend.attributeType

import grails.gorm.services.Service

@Service(PatientAttributeType)
interface PatientAttributeTypeService {

    PatientAttributeType get(Serializable id)

    List<PatientAttributeType> list(Map args)

    Long count()

    PatientAttributeType delete(Serializable id)

    PatientAttributeType save(PatientAttributeType attributeType)

}
