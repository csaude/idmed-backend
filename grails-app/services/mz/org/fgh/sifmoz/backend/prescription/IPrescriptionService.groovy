package mz.org.fgh.sifmoz.backend.prescription

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.service.ClinicalService


interface IPrescriptionService {

    Prescription get(Serializable id)

    List<Prescription> list(Map args)

    Long count()

    Prescription delete(Serializable id)

    Prescription save(Prescription prescription)

    List<Prescription> getAllByClinicId(String clinicId, int offset, int max)

    Prescription getByVisitIds(String pvdsId, int offset, int max)

    List<Prescription> getAllLastPrescriptionOfClinic(String clinicId, int offset, int max)

    Map<String ,Prescription> getLastPrescriptionsByClinicAndClinicalService(Clinic clinic, ClinicalService clinicalService)

    Map<String ,PatientVisitDetails> getLastPrescriptionsByClinicAndClinicalServiceAndEndDate(Clinic clinic, ClinicalService clinicalService, Date endDate)

    Prescription getLastPrescriptionByPatientId(String patientId)
}
