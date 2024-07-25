package mz.org.fgh.sifmoz.backend.patient

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams


interface IPatientService {

    Patient get(Serializable id)

    List<Patient> list(Map args)

    Long count()

    Patient delete(Serializable id)

    Patient save(Patient patient)

    List<Patient> search(Patient patient,int offset, int limit)

    List<Patient> search(String searchString, String clinicId)

    List<Patient> getAllByClinicId(String clinicId, int offset, int max)

    Long count(Patient patient)

    List getPatientWithoutDispense(ReportSearchParams reportSearchParams)

    List<Patient> getAllPatientsInClinicSector(Clinic clinicSector, int offset, int limit)

    List getAllExpectedPatients(ReportSearchParams reportSearchParams)
  
    Long countPatientSearchResult(Patient patient)

    List findPossibleDuplicatePatients()

    List<Patient> getAllPatientsIsAbandonment(int offset, int max)

}
