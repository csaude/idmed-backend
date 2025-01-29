package mz.org.fgh.sifmoz.backend.patient

import com.fasterxml.jackson.databind.JsonNode
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


    static lazyInit = false

    @Scheduled(cron = "0 0 14 * * *")
    void schedulerRequestRunning() {

         Patient.withTransaction {
            List<InteroperabilityAttribute> interoperabilityAttributes = InteroperabilityAttribute.findAll()
            if (!interoperabilityAttributes.isEmpty()) {
                println "Iniciando a Rotina de Busca de Pacientes para Actualizacao"
                String universalProviderUUid = interoperabilityAttributes.find { it.interoperabilityType.code == "UNIVERSAL_PROVIDER_UUID" }.value
                String urlBase = interoperabilityAttributes.find { it.interoperabilityType.code == "URL_BASE" }.value

                try {
                   // RestOpenMRSClient restPost = new RestOpenMRSClient()
                    String urlPath = "patient/info/updated-data?client_name=eip"
                    def response =  RestOpenMRSClient.getResponseOpenMRSClient(universalProviderUUid, null, urlBase ,urlPath, requestMethod_GET)

                    if (response?.entry) {
                        response.entry.each { patient ->
                            Map<String, String> identifierMap = extractIdentifiers(patient.identifier)
                            String nid = identifierMap.entrySet().stream()
                                    .filter(entry -> entry.getKey().contains("nid-tarv"))
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                                    .orElse(null);

                            String uuid = identifierMap.entrySet().stream()
                                    .filter(entry -> entry.getKey().contains("patient-uuid"))
                                    .map(Map.Entry::getValue)
                                    .findFirst()
                                    .orElse(null);

                            if (nid) {
                                def idmedPatient = findPatientToUpdate(uuid, nid)
                                if (idmedPatient) {
                                    populatePatientDetails(idmedPatient, patient)
                                    patientService.save(idmedPatient)
                                }
                            }
                        }
                    }

                    String commitUrlPath = "patient/info/updated-data/commit?client_name=eip"
                    RestOpenMRSClient.getResponseOpenMRSClient(universalProviderUUid, null, urlBase ,commitUrlPath, requestMethod_POST)

                } catch (Exception e) {
                    e.printStackTrace()
                }
            }
             }

    }



    private Map<String, String> extractIdentifiers(List<JSONObject> identifiers) {
        Map<String, String> identifierMap = [:]
        identifiers.each { identifierNode ->
            identifierMap[identifierNode.system] = identifierNode.value
        }
        return identifierMap
    }

    // Helper method to find or create a patient by identifier
    private Patient findPatientToUpdate(String uuid, String nid) {
        def patientIdentifier = PatientServiceIdentifier.findByValue(nid)
        if (patientIdentifier) {
            return patientIdentifier.patient
        } else {
            def idmedPatient = Patient.findByHisUuid(uuid)
            if (idmedPatient) {
                idmedPatient.identifiers.each { item ->
                    if (item.service.code == 'TARV') {
                        item.value = nid
                    }
                }
            }
            return idmedPatient
        }
    }


    private void populatePatientDetails(Patient idmedPatient, Map patient) {
        patient.name.each { name ->
            idmedPatient.firstNames = name.given[0]
            idmedPatient.middleNames = name.given[1]
            idmedPatient.lastNames = name.family
        }

        idmedPatient.gender = patient.gender == 'M' ? 'Masculino' : 'Feminino'
        if (patient.birthDate) {
            idmedPatient.dateOfBirth = ConvertDateUtils.createDate(patient.birthDate, "yyyy-MM-dd")
        }

        if (patient.address?.size() >= 1) {
            idmedPatient.province = Province.findByDescription(patient.address[0].state)
            idmedPatient.address = patient.address[0].line[0]
            idmedPatient.addressReference = patient.address[0].line[3]
        }

        if (patient.telecom?.size() >= 1) {
            idmedPatient.cellphone = patient.telecom[0].value
            idmedPatient.alternativeCellphone = patient.telecom.size() > 1 ? patient.telecom[1]?.value : null
        }

        handleExtensions(idmedPatient, patient.extension)
    }


    private void handleExtensions(Patient idmedPatient, List extensions) {
        if (extensions) {

            String rootUrl = extensions.get(0).url
            Map<String, Object> groupedData = [:] // Map to hold valueCode and valueDate

            if (extensions.get(0)) {
                extensions.get(0).extension.each { nestedNode ->
                   // String nestedUrl = nestedNode.url
                    if (nestedNode.containsKey("valueCode")) {
                        groupedData.put("valueCode", nestedNode.valueCode)
                    }
                    if (nestedNode.containsKey("valueDate")) {
                        groupedData.put("valueDate", nestedNode.valueDate)
                    }
                }
            }

                switch (groupedData.valueCode) {
                    case 'ABANDONO':
                    case 'SUSPENSO':
                        episodeService.closeEpisodeWhenOpenmrsStatusCodeAbandonAndSuspended(idmedPatient, String.valueOf(groupedData.valueCode),ConvertDateUtils.createDate(groupedData.valueDate,'yyyy-MM-dd'))
                        break
                    case 'OBITO':
                    case 'TRANSFERIDO_PARA':
                        episodeService.closePatientServiceIdentifierOfPatientWhenOpenMrsObitOrTransferred(idmedPatient, String.valueOf(groupedData.valueCode),ConvertDateUtils.createDate(groupedData.valueDate,'yyyy-MM-dd'))
                        break
                    default:
                        println "Unhandled extension valueCode: ${groupedData.valueCode}"
                        break
            }
        } else {
            episodeService.reopenEpisodeAndServiceWhenPatientActiveInSesp(idmedPatient)
        }
    }
}