package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Transactional
@EnableScheduling
@Slf4j
class RestGetEpisodeCentralMobileService extends SynchronizerTask {

    @Autowired
    IPatientVisitDetailsService visitDetailsService
    IEpisodeService episodeService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()

    static lazyInit = false

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataGetEpisode");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Episodio",
            "patientUuid");

//    @Scheduled(fixedDelay = 90000L)
    void execute() {

        if (this.instalationConfig != null && !this.isProvincial()) {

            Clinic clinic = Clinic.findById(this.getUsOrProvince())
            ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinic.getProvince().code, MOBILE_SERVER)
            String urlPath = "/sync_temp_episode?usuuid=eq." + clinic.getUuid() + "&syncstatus=eq.S"; // addClinicUUid
            LOGGER.info("Iniciando a Busca de Episodios de Fim")
            def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath)

            ClinicalService clinicalService = ClinicalService.findByCode("TARV")
            StartStopReason startStopReason = StartStopReason.findByCode("VOLTOU_REFERENCIA")
            EpisodeType episodeType = EpisodeType.findByCode("INICIO")

            LOGGER.info(MESSAGE)
            for (Object episode : response) {
                try {
                    String message = " Episodio sem Identificador."
                    if(episode.getAt("id") != null && episode.getAt("patientuuid") != null){
                        message = String.format(FORMAT_STRING, episode.getAt("id").toString(), episode.getAt("patientuuid").toString())
                    }

                    LOGGER.info("Processando" + message)

                    //GetPatientClinicService
                    Patient patient = Patient.findByHisUuid(episode.getAt('patientuuid').toString())
                    PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findByPatientAndService(patient, clinicalService)

                    // Episode lastEpisode = Episode.findByPatientServiceIdentifier(patientServiceIdentifier,[sort: 'episodeDate', order: 'desc'])
                    if (patient != null || patientServiceIdentifier != null) {
                        Episode backReferenceEpisode = new Episode()
                        backReferenceEpisode.setEpisodeDate(ConvertDateUtils.createDate(episode.getAt("startdate").toString(), "yyyy-MM-dd"))
                        backReferenceEpisode.setEpisodeType(episodeType)
                        backReferenceEpisode.setStartStopReason(startStopReason)
                        backReferenceEpisode.setPatientServiceIdentifier(patientServiceIdentifier)
                        backReferenceEpisode.setNotes(episode.getAt("startreason").toString())
                        backReferenceEpisode.setClinic(Clinic.findByUuid(episode.getAt("usuuid").toString()))
                        backReferenceEpisode.setClinicSector(ClinicSector.findByCode("TARV"))
                        //Correct
                        backReferenceEpisode.setCreationDate(new Date())
                        episodeService.save(backReferenceEpisode)

                        //Send the episode to Update - Patch Server

                        def path = "/sync_temp_episode?id=eq." + episode.getAt("id")
                        String obj = '{"syncstatus":"U"}'
                        println(obj)

                        restProvincialServerClient.patchRequestProvincialServerClient(provincialServer, path, obj)
                    } else {
                        LOGGER.info("Patient com uuid:+" + episode.getAt("patientuuid").toString() + " nao encontrado");
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }finally {
                    continue
                }

            }
        }
    }
}
