package mz.org.fgh.sifmoz.backend.patientIdentifier

import grails.gorm.services.Service


interface IPatientServiceIdentifierService {

    PatientServiceIdentifier get(Serializable id)

    List<PatientServiceIdentifier> list(Map args)

    Long count()

    PatientServiceIdentifier delete(Serializable id)

    PatientServiceIdentifier save(PatientServiceIdentifier patientProgramIdentifier)

    List<PatientServiceIdentifier> getAllByClinicId(String clinicId, int offset, int max)

    List<PatientServiceIdentifier> getAllByPatientId(String patientId, int offset, int max)

    List<PatientServiceIdentifier> getAllByPatientId(String serviceId)

}
