package mz.org.fgh.sifmoz.backend.packaging


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.restUtils.RestOpenMRSClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
@CompileStatic
@EnableScheduling
class RestPackService {

    RestOpenMRSClient restOpenMRSClient = new RestOpenMRSClient()
    PatientVisitDetailsService patientVisitDetailsService
    final String requestMethod_POST = "POST"
    final String requestMethod_PUT = "PUT"
    final String requestMethod_PATCH = "PATCH"
    final String requestMethod_GET = "GET"

    final String NO_MEDICATION_ERROR_MESSAGE = "- Dispensa sem Medicamentos\n"
    final String NO_DISPENSE_MODE_ERROR_MESSAGE = "- Dispensa sem Modos de Dispensa\n"
    final String NO_PRESCRIPTION_ERROR_MESSAGE = "- Dispensa sem Prescricao\n"
    final String NO_EPISODE_ERROR_MESSAGE = "- Dispensa sem Historico Clinico\n"
    final String PATIENT_OPENMRS_UUID_MISSING_ERROR_MESSAGE = "- Paciente sem UUID do OpenMRS \n"
    final String PATIENT_OPENMRS_LOCATION_MISSING_ERROR_MESSAGE = "- Paciente sem UUID LOCATION do OpenMRS \n"
    static lazyInit = false

//    @Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {
        Pack.withTransaction {
            List<Pack> packList = Pack.findAllWhere(syncStatus: 'R' as char)

            for (Pack pack : packList) {
                try {
                    RestOpenMRSClient restPost = new RestOpenMRSClient()
                    PatientVisitDetails patientVisitDetails = patientVisitDetailsService.getByPack(pack)
                    PatientVisit patientVisit = PatientVisit.get(patientVisitDetails.patientVisit.id)
                    Patient patient = Patient.get(patientVisit.patient.id)
                    Episode episode = patientVisitDetails?.episode
                    PatientServiceIdentifier patientServiceIdentifier = episode?.patientServiceIdentifier
                    ClinicalService clinicalService = patientServiceIdentifier?.service

                    if (patient.his == null || clinicalService?.code?.equals('PREP')) {
                        pack.setSyncStatus('N' as char)
                        pack.save()
                        return
                    }
                    HealthInformationSystem his = HealthInformationSystem.get(patient.his.id)
                    String urlBase = his.interoperabilityAttributes.find { it.interoperabilityType.code == "URL_BASE" }.value
                    String convertToJson = restPost.createOpenMRSDispense(pack, patient)
                    if (pack.providerUuid == null) {
                        String providerUuid = his.interoperabilityAttributes.find { it.interoperabilityType.code == "OPENMRS_USER_PROVIDER_UUID" }.value
                        pack.providerUuid = providerUuid
                    }
                    String responsePost = restOpenMRSClient.requestOpenMRSClient(pack.providerUuid, convertToJson, urlBase, "encounter", requestMethod_POST)
                    if (responsePost.startsWith('-> Green')) {
                        pack.setSyncStatus('S' as char)
                        pack.save()

                        deleteErrorLog(patientVisitDetails)
                    } else {
                        saveErrorLog(pack, patientVisitDetails, patient, responsePost, convertToJson)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    continue
                }
            }
        }
    }

    def saveErrorLog(Pack pack,  PatientVisitDetails patientVisitDetails, Patient patient, String errorResponse, String jSONRequest) {
        try {
            OpenmrsErrorLog errorLog = new OpenmrsErrorLog()
            errorLog.beforeInsert()
            Episode episode = Episode.get(patientVisitDetails.episode.id)
            PatientServiceIdentifier identifier = PatientServiceIdentifier.get(episode.patientServiceIdentifier.id)
            ClinicalService service = ClinicalService.get(identifier.service.id)
            OpenmrsErrorLog openmrsLogExists = OpenmrsErrorLog.findWhere(patientVisitDetails: patientVisitDetails.id)
            def messageError = findPossibleErrorMessage(pack, patientVisitDetails, patient)

            errorLog.patient = patient.id
            errorLog.nid = identifier.value
            errorLog.servicoClinico = service.description
            errorLog.patientVisitDetails = patientVisitDetails.id
            errorLog.pickupDate = pack.pickupDate
            errorLog.returnPickupDate = null
            errorLog.errorDescription = errorResponse.concat(messageError)
            errorLog.jsonRequest = jSONRequest

            if(pack.nextPickUpDate != null){
                errorLog.returnPickupDate = pack.nextPickUpDate
            }

            if (openmrsLogExists == null)   {
                errorLog.save()
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    String findPossibleErrorMessage(Pack pack,  PatientVisitDetails patientVisitDetails, Patient patient){

        String error = " ERRO: \n"
        def isPackEmpty = pack?.packagedDrugs?.isEmpty()
        def isDispenseModeEmpty = pack?.dispenseMode?.openmrsUuid == null || pack?.dispenseMode?.openmrsUuid?.isEmpty()
        def isPatientOpenMRSUUIDEmpty = patient?.hisUuid == null || patient?.hisUuid?.isEmpty()
        def isPatientOpenMRLocationEmpty = patient?.hisLocation == null || patient?.hisLocation?.isEmpty()
        def isPrescriptionEmpty = patientVisitDetails.prescription == null
        def isEpisodeEmpty = patientVisitDetails.episode == null

        if (isPackEmpty) {
            error = error.concat(NO_MEDICATION_ERROR_MESSAGE)
        }

        if(isDispenseModeEmpty){
            error = error.concat(NO_DISPENSE_MODE_ERROR_MESSAGE)
        }

        if(isPatientOpenMRSUUIDEmpty){
            error = error.concat(PATIENT_OPENMRS_UUID_MISSING_ERROR_MESSAGE)
        }

        if(isPatientOpenMRLocationEmpty){
            error = error.concat(PATIENT_OPENMRS_LOCATION_MISSING_ERROR_MESSAGE)
        }

        if(isPrescriptionEmpty){
            error = error.concat(NO_PRESCRIPTION_ERROR_MESSAGE)
        }

        if(isEpisodeEmpty){
            error = error.concat(NO_EPISODE_ERROR_MESSAGE)
        }

        return error
    }

    def deleteErrorLog(PatientVisitDetails patientVisitDetails) {
        List<OpenmrsErrorLog> patientVisitDetailsinLog = OpenmrsErrorLog.findAllWhere(patientVisitDetails:patientVisitDetails.id)
        patientVisitDetailsinLog.each {it ->
            if (it) {
                it.delete()
            }
        }
    }

}
