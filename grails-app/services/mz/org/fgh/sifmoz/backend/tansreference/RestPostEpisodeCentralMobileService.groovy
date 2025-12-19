package mz.org.fgh.sifmoz.backend.tansreference

import com.google.gson.Gson
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.healthInformationSystem.ISystemConfigsService
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigsService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.apache.http.entity.StringEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Transactional
@EnableScheduling
@Slf4j
class RestPostEpisodeCentralMobileService extends SynchronizerTask {

    private static final NAME = "PostEpisodeCentralMobile"
    @Autowired
    IPatientVisitDetailsService visitDetailsService
    IEpisodeService episodeService
    IPatientService patientService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    @Autowired
    ISystemConfigsService configsService

    static lazyInit = false

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataPostEpisode");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Episodio",
            "Nome",
            "NID");

//    final String EPISODIO_IDMED_IDART_PROVINCIAL_ATIVO = true

    @Scheduled(fixedDelay = 14400000L) // 4 horas após terminar
    void execute() {
        if (configsService.getRotineStatus('EPISODIO_IDMED_IDART_PROVINCIAL_ATIVO')) {
            println " - REST PATIENT EPISODE FROM IDMED TO PROVINCIAL " + new Date()
            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged.getProvince().code, MOBILE_SERVER)
                PatientTransReferenceType patientTransReferenceType = PatientTransReferenceType.findByCode("VOLTOU_DA_REFERENCIA")
                List<PatientTransReference> patientsTransferees = PatientTransReference.findAllBySyncStatusAndOperationType('P', patientTransReferenceType)

                LOGGER.info("Iniciando o Envio de Episodios")
                LOGGER.info(MESSAGE)
                for (PatientTransReference pt : patientsTransferees) {
                    String message = String.format(FORMAT_STRING,
                            pt.matchId,
                            pt.patient.firstNames,
                            pt.identifier.value)
                    LOGGER.info("Processando" + message);
                    try {
                        char syncStatus = 'S'
                        SyncTempEpisode syncTempEpisode = new SyncTempEpisode()

                     //   Episode episode = episodeService.getLastInitialEpisodeByIdentifier(pt.identifier.id)

                        Episode episode = episodeService.getLastEpisodeByIdentifier(pt.patient,pt.identifier.service.code)
                        StartStopReason startStopReason = StartStopReason.findByCode('REFERIDO_PARA')

                        Episode lastReferralEpisode = Episode.findByStartStopReasonAndPatientServiceIdentifier(startStopReason,pt.identifier)
                     //   PatientVisitDetails lastVisitDetails = visitDetailsService.getLastVisitByEpisodeId(episode.id)

                        //Set realData on startDate
                        syncTempEpisode.setId(pt.matchId) //correct
                        syncTempEpisode.setStartdate(pt.operationDate)
                        //  syncTempEpisode.setStartreason("Voltou da Referencia")
                        //   syncTempEpisode.setStartnotes("Voltou da Referencia")
                        syncTempEpisode.setStopdate(pt.operationDate)
                        syncTempEpisode.setStopreason(episode.startStopReason.reason)
                        syncTempEpisode.setStopnotes(episode.notes)
                        syncTempEpisode.setPatientuuid(pt.identifier.patient.hisUuid)  //pt.identifier.patient.hisUuid
                        syncTempEpisode.setClinicuuid(lastReferralEpisode?.referralClinic?.uuid) //pt.identifier.clinic.uuid
                        syncTempEpisode.setUsuuid(pt.identifier.clinic.uuid)
                        syncTempEpisode.setSyncstatus(syncStatus)

                        def obj = Utilities.parseToJSON(syncTempEpisode)
                        println(obj)
                        def response = restProvincialServerClient.postRequestProvincialServerClient(provincialServer, "/sync_temp_episode", obj)
                        // destination passou a ser uuid de fp ou dispensa comunitaria
                        if (Integer.parseInt(response) == HttpURLConnection.HTTP_CREATED) {
                            pt.syncStatus = syncStatus
                            patientTransReferenceService.save(pt)
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        continue
                    }
                }
            }
        }
    }

}
