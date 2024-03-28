package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
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

    static lazyInit = false

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataPostEpisode");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Episodio",
            "Nome",
            "NID");

    @Scheduled(fixedDelay = 90000L)
    void execute() {
        if (this.instalationConfig != null && !this.isProvincial()) {
        Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
        ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged.getProvince().code, MOBILE_SERVER)
            PatientTransReferenceType patientTransReferenceType = PatientTransReferenceType.findByCode("VOLTOU_DA_REFERENCIA")
            List<PatientTransReference> patientsTransferees = PatientTransReference.findAllBySyncStatusAndOperationType('P',patientTransReferenceType)

            LOGGER.info("Iniciando o Envio de Episodios" )
            LOGGER.info(MESSAGE)
            for (PatientTransReference pt : patientsTransferees) {
                String message = String.format(FORMAT_STRING,
                        pt.matchId ,
                        pt.patient.firstNames,
                        pt.identifier.value)
                LOGGER.info("Processando" +message);
                try {
                    char syncStatus = 'S'
                    SyncTempEpisode syncTempEpisode = new SyncTempEpisode()

                    Episode episode = episodeService.getLastInitialEpisodeByIdentifier(pt.identifier.id)
                    PatientVisitDetails lastVisitDetails = visitDetailsService.getLastVisitByEpisodeId(episode.id)

                    //Set realData on startDate
                    syncTempEpisode.setId(pt.matchId) //correct
                    syncTempEpisode.setStartdate(pt.operationDate)
                 //  syncTempEpisode.setStartreason("Voltou da Referencia")
                //   syncTempEpisode.setStartnotes("Voltou da Referencia")
                    syncTempEpisode.setStopdate(pt.operationDate)
                    syncTempEpisode.setStopreason(episode.startStopReason.reason)
                    syncTempEpisode.setStopnotes(episode.notes)
                    syncTempEpisode.setPatientuuid(pt.identifier.patient.hisUuid)  //pt.identifier.patient.hisUuid
                    syncTempEpisode.setClinicuuid(pt.identifier.clinic.uuid) //pt.identifier.clinic.uuid
                    syncTempEpisode.setUsuuid(pt.identifier.clinic.uuid)
                    syncTempEpisode.setSyncstatus(syncStatus)

                    def obj = Utilities.parseToJSON(syncTempEpisode)
                    println(obj)
                  def response =  restProvincialServerClient.postRequestProvincialServerClient(provincialServer,"/sync_temp_episode",obj)
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
