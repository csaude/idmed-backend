package mz.org.fgh.sifmoz.backend.patient

import com.google.gson.JsonObject
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientIdentifier.IPatientServiceIdentifierService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.restUtils.RestOpenMRSClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import org.apache.commons.lang.StringUtils
import org.grails.web.json.JSONArray
import org.grails.web.json.JSONObject
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
@CompileStatic
@EnableScheduling
class RestPatientService {


    RestOpenMRSClient restOpenMRSClient = new RestOpenMRSClient()
    IPatientService patientService
    final String requestMethod_POST = "POST"
    IPatientServiceIdentifierService patientServiceIdentifier;

    static lazyInit = false

    @Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {

        Patient.withTransaction {

            List<InteroperabilityAttribute> interoperabilityAttributes = InteroperabilityAttribute.findAll()
            if (!interoperabilityAttributes.isEmpty()) {
                String hisLocation = interoperabilityAttributes.find { it.interoperabilityType.code == "OPENMRS_LOCATION_UUID" }.value
                String identifierTypeIdOpenMrs = interoperabilityAttributes.find { it.interoperabilityType.code == "PATIENT_IDENTIFIER_TYPE_NID_UUID" }.value

                def patients = Patient.executeQuery("select p from PatientServiceIdentifier psi " +
                        " inner join psi.patient p " +
                        " inner join psi.service cs " +
                        " where cs.code  like 'TARV' and  p.hisSyncStatus like 'P' ")

                for (Patient patient in (List<Patient>) patients) {
                    patient.hisLocation = hisLocation
                    if (patient.his == null) {
                        patient.setHisSyncStatus('N' as char)
                        patient.save()
                        return
                    }
                    try {
                        RestOpenMRSClient restPost = new RestOpenMRSClient()
                        HealthInformationSystem his = HealthInformationSystem.get(patient.his.id)
                        String urlBase = his.interoperabilityAttributes.find { it.interoperabilityType.code == "URL_BASE" }.value
                        List<ClinicalService> services = ClinicalService.executeQuery("select cs from ClinicalService cs " +
                                " where cs.code = :code ", [code: "TARV"])

                        List<PatientServiceIdentifier> psiList = (List<PatientServiceIdentifier>) PatientServiceIdentifier.executeQuery("select psi from PatientServiceIdentifier psi " +
                                " where psi.patient = :patient and  psi.service = :service  ", [patient: patient, service: services.get(0)])

                        String convertToJson = restPost.createOpenMRSPatient(patient, psiList.get(0), identifierTypeIdOpenMrs)
                        JSONObject responsePost = (JSONObject) restOpenMRSClient.getPatientResponseOpenMRSClient(patient.hisProvider, convertToJson, urlBase, "patient", requestMethod_POST)
                        if (responsePost != null) {
                            String patientUuid = String.valueOf(responsePost.get("uuid"));
                            patient.setHisSyncStatus('S' as char)
                            patient.setHisUuid(patientUuid)
                            patient.save()
                            def packs = (List<Pack>) Pack.executeQuery("select pck from PatientVisitDetails pvd  " +
                                    " inner join pvd.patientVisit pv " +
                                    " inner join pvd.pack pck where pv.patient = :patient", [patient: patient])
                            for (Pack pack in packs) {
                                pack.setSyncStatus('R' as char)
                                pack.save()
                            }
                        } else {
                            continue
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
