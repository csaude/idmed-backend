package mz.org.fgh.sifmoz.backend.patient

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientIdentifier.IPatientServiceIdentifierService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.restUtils.RestOpenMRSClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import org.grails.web.json.JSONObject
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled


@Slf4j
//@CompileStatic
@EnableScheduling
class RestPatientUpdateService {


    RestOpenMRSClient restOpenMRSClient = new RestOpenMRSClient()
    IPatientService patientService
    IEpisodeService episodeService
    final String requestMethod_GET = "GET"
    final String requestMethod_POST = "POST"
    IPatientServiceIdentifierService patientServiceIdentifier

    static lazyInit = false

    //@Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {

        Patient.withTransaction {
            List<InteroperabilityAttribute> interoperabilityAttributes = InteroperabilityAttribute.findAll()
            if (!interoperabilityAttributes.isEmpty()) {

                String universalProviderUUid = interoperabilityAttributes.find { it.interoperabilityType.code == "UNIVERSAL_PROVIDER_UUID" }.value
                String urlBase = interoperabilityAttributes.find { it.interoperabilityType.code == "URL_BASE" }.value

                try {
                    RestOpenMRSClient restPost = new RestOpenMRSClient()
                    String urlPath = "patient/info/updated-data?client_name=IDMED"
                    def response = RestOpenMRSClient.getResponseOpenMRSClient(universalProviderUUid, null, urlBase, urlPath, "GET")

                    if (response?.entry) {
                        response.entry.each { patient ->
                            patient.identifier.each { identifier ->
                                def identifierValue = identifier.value
                                println(identifierValue)
                                PatientServiceIdentifier patientIdentifier = PatientServiceIdentifier.findByValue(identifierValue)
                                if (patientIdentifier != null) {
                                    Patient idmedPatient = patientIdentifier.patient
                                    println(idmedPatient)
                                    patient.name.each { name ->
                                        idmedPatient.firstNames = name.given[0]
                                        idmedPatient.middleNames = name.given[1]
                                        idmedPatient.lastNames = name.family
                                    }
                                    idmedPatient.gender = patient.gender == 'M' ? 'Masculino' : 'Feminino'
                                    idmedPatient.dateOfBirth = ConvertDateUtils.createDate(patient.birthDate, "yyyy-MM-dd")
                                    if (patient.address.size() >= 1) {
                                        idmedPatient.province = Province.findByDescription(patient.address[0].state)
                                        //  idmedPatient.district = District.findByDescription(patient.address[0].district)
                                        idmedPatient.address = patient.address[0].line[0]
                                        idmedPatient.addressReference = patient.address[0].line[3]
                                    }
                                    if (patient.telecom.size() >= 1) {
                                        idmedPatient.cellphone = patient.telecom[0].value
                                        idmedPatient.alternativeCellphone = patient.telecom[1] != null ? patient.telecom[1].value : null
                                    }
                                    if (patient.deceasedBoolean == true) {
                                        episodeService.closePatientServiceIdentifierOfPatientWhenOpenMrsObit(idmedPatient)
                                    }
                                    patientService.save(idmedPatient)
                                }
                            }
                        }
                        String commitUrlPath = "patient/info/updated-data/commit?client_name=IDMED"
                        RestOpenMRSClient.getResponseOpenMRSClient(universalProviderUUid, null, urlBase, commitUrlPath, "POST")
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
        }
    }

}