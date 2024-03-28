package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReferenceType

@Service(PatientTransReferenceType)
interface PatientTransReferenceTypeService {

    PatientTransReferenceType get(Serializable id)

    List<PatientTransReferenceType> list(Map args)

    Long count()

    PatientTransReferenceType delete(Serializable id)

    PatientTransReferenceType save(PatientTransReferenceType patientTransReferenceType)

}
