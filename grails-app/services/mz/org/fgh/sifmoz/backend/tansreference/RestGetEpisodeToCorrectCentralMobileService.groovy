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
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Transactional
@EnableScheduling
@Slf4j
class RestGetEpisodeToCorrectCentralMobileService extends SynchronizerTask {

    IEpisodeService episodeService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetEpisodeToCorrectCentralMobile"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataGetEpisodeCorrection");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Paciente",
            "Nome",
            "NID");

    static lazyInit = false

    @Scheduled(fixedDelay = 90000L)
    void execute() {
        if (this.instalationConfig != null && !this.isProvincial()) {

            Clinic clinic = Clinic.findById(this.getUsOrProvince())
            ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinic.getProvince().code, MOBILE_SERVER)
            String urlPath = "/sync_temp_patients?mainclinicuuid=eq." + clinic.getUuid() + "&modified=eq.T";
            //addClinicuuid
            def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath)
            LOGGER.info("Iniciando a Busca de Pacientes Para Corrigir")

            StartStopReason startStopReasonBack = StartStopReason.findByCode("VOLTOU_REFERENCIA")
            StartStopReason startStopReasonReferred = StartStopReason.findByCode("REFERIDO_PARA")
            EpisodeType episodeTypeInitial = EpisodeType.findByCode("INICIO")
            EpisodeType episodeTypeEnding = EpisodeType.findByCode("FIM")

            LOGGER.info(MESSAGE)
            /// Object[] dispenses = response
            for (Object patient : response) {
                try {
                    String message = " Paciente sem Identificador."
                    if (patient.getAt("id") != null) {
                        message = String.format(FORMAT_STRING,
                                patient.getAt("id").toString(),
                                patient.getAt("patientfirstname").toString(),
                                patient.getAt("patientid").toString())
                    }
                    LOGGER.info("Processando" + message)

                    if (patient.getAt('patientid') != null) {
                        PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findByValue(patient.getAt('patientid').toString())
                        if (patientServiceIdentifier != null) {
                            Episode backReferenceEpisode = new Episode()
                            backReferenceEpisode.setEpisodeDate(ConvertDateUtils.createDate(patient.getAt("prescriptiondate").toString(), "yyyy-MM-dd"))
                            backReferenceEpisode.setEpisodeType(episodeTypeInitial)
                            backReferenceEpisode.setStartStopReason(startStopReasonBack)
                            backReferenceEpisode.setPatientServiceIdentifier(patientServiceIdentifier)
                            backReferenceEpisode.setNotes(startStopReasonBack.reason)
                            backReferenceEpisode.setClinic(Clinic.findByUuid(patient.getAt("mainclinicuuid").toString()))
                            backReferenceEpisode.setClinicSector(ClinicSector.findByCode("TARV"))
                            // to do get clinic
                            backReferenceEpisode.setCreationDate(new Date())
                            episodeService.save(backReferenceEpisode)

                            Episode newReferenceEpisode = new Episode()
                            newReferenceEpisode.setEpisodeDate(ConvertDateUtils.createDate(patient.getAt("prescriptiondate").toString(), "yyyy-MM-dd"))
                            newReferenceEpisode.setEpisodeType(episodeTypeEnding)
                            newReferenceEpisode.setStartStopReason(startStopReasonReferred)
                            newReferenceEpisode.setPatientServiceIdentifier(patientServiceIdentifier)
                            newReferenceEpisode.setNotes(startStopReasonReferred.reason)
                            newReferenceEpisode.setClinic(Clinic.findByUuid(patient.getAt("mainclinicuuid").toString()))
                            newReferenceEpisode.setClinicSector(ClinicSector.findByCode("TARV"))
                            // to do get clinic
                            Clinic newRefferalClinic = Clinic.findByUuid(patient.getAt("clinicuuid").toString())
                            newReferenceEpisode.setReferralClinic(newRefferalClinic)
                            backReferenceEpisode.setCreationDate(new Date())
                            episodeService.save(newReferenceEpisode)
                            //Send the patient to Update modified flag - Patch Server

                            def path = "/sync_temp_patients?id=eq." + patient.getAt("id")
                            //   patientEdit.put("modified", "F")
                            String obj = '{"modified":"F"}'

                            restProvincialServerClient.patchRequestProvincialServerClient(provincialServer, path, obj)
                        } else {
                            LOGGER.info("Servico de Saude Nao encontrado Para o paciente com o nid:" + patient.getAt("patientid").toString());
                        }
                    } else {
                        LOGGER.error("Paciente sem Identificador")
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
