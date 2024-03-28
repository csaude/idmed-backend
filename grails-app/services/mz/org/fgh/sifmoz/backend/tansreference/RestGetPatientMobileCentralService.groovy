package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.episode.EpisodeService
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.IPatientServiceIdentifierService
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
class RestGetPatientMobileCentralService extends SynchronizerTask {

    private static final NAME = "GetPatientMobileCentralMobile"
    @Autowired
    IPatientVisitDetailsService visitDetailsService
    EpisodeService episodeService
    IPatientService patientService
    IPatientServiceIdentifierService patientServiceIdentifierService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()

    static lazyInit = false

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataGetNewPatient");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Paciente",
            "Nome",
            "NID");

    @Scheduled(fixedDelay = 90000L)
    void execute() {

        if (this.instalationConfig != null && !this.isProvincial()) {
            Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
            ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged.getProvince().code, MOBILE_SERVER)
            String urlPath = "/sync_mobile_patient?clinicuuid=eq." + clinicLoged.getUuid() + "syncstatus=eq.S";
            //addClinicuuid

            LOGGER.info("Iniciando a Busca de Novos Pacientes")
            def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath)

            StartStopReason startStopReasonIni = StartStopReason.findByCode("Inicio ao tratamento")
            EpisodeType episodeTypeInitial = EpisodeType.findByCode("INICIO")

            LOGGER.info(MESSAGE)
            for (Object patient : response) {
                try {
                    String message = "Paciente sem Identificador"
                    if (patient.getAt("id") != null) {
                        message = String.format(FORMAT_STRING,
                                patient.getAt("id").toString(),
                                patient.getAt("patientfirstname").toString(),
                                patient.getAt("patientid").toString())
                    }

                    LOGGER.info("Processando" + message)

                    Patient newPatient = new Patient()
                    newPatient.setFirstNames(patient.getAt("firstnames"))
                    newPatient.setLastNames(patient.getAt("lastname"))
                    newPatient.setCellphone(patient.getAt("cellphone"))
                    newPatient.setDateOfBirth(ConvertDateUtils.createDate(patient.getAt("dateofbirth").toString(), "yyyy-MM-dd"))
                    newPatient.setAddress(patient.getAt("address2"))
                    newPatient.setProvince(Province.findByDescription(patient.getAt("province").toString()))
                    newPatient.setDistrict(District.findByDescription(patient.getAt("address1").toString()))
                    if (patient.getAt("sex").toString().equalsIgnoreCase('M')) {
                        char charM = 'M'
                        newPatient.setGender(charM)
                    } else {
                        char charF = 'F'
                        newPatient.setGender(charF)
                    }
                    Clinic clinic = Clinic.findByUuid(patient.getAt("clinicuuid").toString())
                    newPatient.setClinic(clinic)

                    Patient createdPatient = patientService.save(newPatient)

                    PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findByValue(dispense.getAt('patientid'))

                    patientServiceIdentifier.setClinic(clinic)
                    patientServiceIdentifier.setStartDate(ConvertDateUtils.createDate(patient.getAt("arvstartdate").toString(), "yyyy-MM-dd"))
                    patientServiceIdentifier.setPatient(createdPatient)
                    patientServiceIdentifier.setValue(patient.getAt("patientid").toString())
                    patientServiceIdentifier.setPrefered(true)
                    patientServiceIdentifier.setService(ClinicalService.findByCode("TARV"))

                    PatientServiceIdentifier createdPatientSI = patientServiceIdentifierService.save(patientServiceIdentifier)

                    Episode episode = new Episode()
                    episode.setPatientServiceIdentifier(createdPatientSI)
                    episode.setClinic(clinic)
                    episode.setClinicSector(ClinicSector.findByUuid(patient.getAt("clinicsectoruuid").toString()))
                    episode.setEpisodeDate(ConvertDateUtils.createDate(patient.getAt("enrolldate").toString(), "yyyy-MM-dd"))
                    episode.setEpisodeType(episodeTypeInitial)
                    episode.setStartStopReason(startStopReasonIni)

                    Episode createdPatientEp = episodeService.save(episode)

                    //Send the patient to Update modified flag - Patch Server
                    def path = "/sync_mobile_patient?id=eq." + patient.getAt("id")
                    String obj = '{"syncstatus":"U"}'
                    println(obj)

                    restProvincialServerClient.patchRequestProvincialServerClient(provincialServer, path, obj)
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    continue
                }
            }
        }
    }
}
