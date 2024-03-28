package mz.org.fgh.sifmoz.backend.episode

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.duration.Duration
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.service.ClinicalService

interface IEpisodeService {

    Episode get(Serializable id)

    List<Episode> list(Map args)

    Long count()

    Episode delete(Serializable id)

    Episode save(Episode episode)

    List<Episode> getAllByClinicId(String clinicId, int offset, int max)

    List<Episode> getAllByIndentifier(String identifierId, int offset, int max)

    List<Episode> getEpisodeOfReferralOrBackReferral(Clinic clinic, ClinicalService clinicalServiceId, String startStopReasonCode, Date startDate, Date endDate)

    Episode getEpisodeOfReferralByPatientServiceIdentfierAndBelowEpisodeDate(PatientServiceIdentifier patientServiceIdentifier, Date episodeDate)

    Episode getLastInitialEpisodeByIdentifier(String identifierId)

    Episode getLastWithVisitByIndentifier(PatientServiceIdentifier patientServiceIdentifier, Clinic clinic)

    Episode getLastEpisodeByIdentifier(Patient patient, String serviceCode)

    List<Episode> getLastWithVisitByClinicAndClinicSector(ClinicSector clinicSector)
}
