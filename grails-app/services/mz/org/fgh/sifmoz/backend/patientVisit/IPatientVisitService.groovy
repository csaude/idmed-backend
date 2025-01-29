package mz.org.fgh.sifmoz.backend.patientVisit


interface IPatientVisitService {

    PatientVisit get(Serializable id)

    List<PatientVisit> list(Map args)

    Long count()

    PatientVisit delete(Serializable id)

    PatientVisit save(PatientVisit visit)

    List<PatientVisit> getAllByPatientId(String patientId)

    List<PatientVisit> getAllByClinicId(String clinicId, int offset, int max)

    PatientVisit getLastVisitOfPatient(String patientId)

    List<PatientVisit> getAllLastWithScreening(String clinicId, int offset, int max)

    List<PatientVisit> getAllLastWithScreeningByPatientIds(List<String> patientIds)

    List<PatientVisit> getAllLast3VisitsWithScreeningByPatientIds(List<String> patientIds)


}
