package mz.org.fgh.sifmoz.backend.patientVisitDetails

import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation.DrugQuantityTemp

interface IPatientVisitDetailsService{

    PatientVisitDetails get(Serializable id)

    List<PatientVisitDetails> list(Map args)

    Long count()

    PatientVisitDetails delete(Serializable id)

    PatientVisitDetails save(PatientVisitDetails patientVisitDetails)

    List<PatientVisitDetails> getAllByClinicId(String clinicId, int offset, int max)

    List<PatientVisitDetails> getAllByEpisodeId(String episodeId, int offset, int max)

    List<PatientVisitDetails> getAllLastVisitOfClinic(String clinicId, int offset, int max)

    PatientVisitDetails getByPack(Pack pack)

    List<PatientVisitDetails> getARVDailyReport(String clinicId, Date startDate, Date endDate, String clinicalServiceId)

    List<DrugQuantityTemp> getProducts(String patientVisitDetailId, String clinicId)

    PatientVisitDetails getLastVisitByEpisodeId(String episodeId)

    PatientVisitDetails getLastByEpisodeId(String episodeId)

    List<PatientVisitDetails> getAllByPatientId(String patientId)

    List<PatientVisitDetails> getLastAllByListPatientId(List<String> patientIds)


    // NOVOS REPORTS
    List<PatientVisitDetails> getPREPDailyReport(String clinicId, Date startDate, Date endDate, String clinicalServiceId)
    List<PatientVisitDetails> getTPTDailyReport(String clinicId, Date startDate, Date endDate, String clinicalServiceId)
}
