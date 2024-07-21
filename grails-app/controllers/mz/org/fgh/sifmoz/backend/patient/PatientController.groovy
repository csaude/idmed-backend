package mz.org.fgh.sifmoz.backend.patient

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Localidade
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.LocalidadeService
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.interoperabilityType.InteroperabilityType
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifierService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.restUtils.RestOpenMRSClient
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.grails.web.json.JSONObject
import org.hibernate.SessionFactory

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

class PatientController extends RestfulController {

    IPatientService patientService
    LocalidadeService localidadeService
    PatientServiceIdentifierService patientServiceIdentifierService

    def SessionFactory sessionFactory

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientController() {
        super(Patient)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientService.list(params)) as JSON
    }

    def show(String id) {
        Patient patient = patientService.get(id)
        render JSONSerializer.setJsonObjectResponse(patient) as JSON
        //respond patientService.get(id)
    }

    @Transactional
    def save() {
        Patient patient = new Patient()
        Clinic clinic = Clinic.findByMainClinic(true)
        def objectJSON = request.JSON
        patient = objectJSON as Patient
        patient.identifiers = [].withDefault { new PatientServiceIdentifier() }
        patient.clinic = clinic
        patient.beforeInsert()

        if (patient.getMatchId() == null) {
            def  patientAux =  Patient.findAll( [max: 2, sort: ['matchId': 'desc']])
            if (patientAux.size() == 0) {
                patient.setMatchId(1)
            } else {
                patient.setMatchId(patientAux.get(0).matchId + 1)
            }
        }
        patient.validate()

        if (objectJSON.id) {
            patient.id = UUID.fromString(objectJSON.id)
        }

        if (patient.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patient.errors
            return
        }

        try {
            if (patient.bairro) {
                Localidade localidade = Localidade.findByCode(patient.bairro.code)
                if (!localidade) {
                    patient.bairro.id = objectJSON.bairro.id
                    localidadeService.save(patient.bairro)
                }
            }

            patientService.save(patient)

            (objectJSON.identifiers as List).collect { item ->
                if (item) {
                    def identifier = new PatientServiceIdentifier(item as Map)
                    identifier.patient = null
                    identifier.patient = patient
                    identifier.id = item.id
                    patientServiceIdentifierService.save(identifier)
                    patient.addToIdentifiers(identifier)
                }

            }

        } catch (ValidationException e) {
            respond patient.errors
            return
        }


        render JSONSerializer.setJsonObjectResponse(patient) as JSON
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        def patientFromJSON = (parseTo(objectJSON.toString()) as Map) as Patient
        Patient patient = Patient.get(objectJSON.id)
//        bindData(patient, patientFromJSON, [exclude: ['id', 'clinicId', 'his', 'hisId', 'provinceId', 'districtId', 'bairroId', 'clinic', 'attributes', 'appointments', 'patientTransReference', 'validated', 'postoAdministrativoId','matchId', 'entity']])


        patient.firstNames = patientFromJSON.firstNames
        patient.middleNames = patientFromJSON.middleNames
        patient.lastNames = patientFromJSON.lastNames
        patient.gender = patientFromJSON.gender
        patient.dateOfBirth = patientFromJSON.dateOfBirth
        patient.cellphone = patientFromJSON.cellphone
        patient.alternativeCellphone = patientFromJSON.alternativeCellphone
        patient.address = patientFromJSON.address
        patient.addressReference = patientFromJSON.addressReference
        patient.province = patientFromJSON.province
        patient.bairro = patientFromJSON.bairro
        patient.district = patientFromJSON.district
        patient.postoAdministrativo = patientFromJSON.postoAdministrativo
        patient.hisUuid = patientFromJSON.hisUuid

        List<PatientServiceIdentifier> identifiersList = new ArrayList<>()

        /*   if (patient.identifiers != null) {
               patient.identifiers = [].withDefault { new PatientServiceIdentifier() }

               (objectJSON.identifiers as List).collect { item ->
                   if (item) {
                       def identifier = PatientServiceIdentifier.get(item.id)
                       identifier.patient = patient
                       identifiersList.add(identifier)
                   }
               }
               patient.identifiers = identifiersList
           }*/
        if (patient == null) {
            render status: NOT_FOUND
            return
        }

        if (patient.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patient.errors
            return
        }

        try {
            if (patient.bairro) {
                Localidade localidade = Localidade.findByCode(patient.bairro.code)
                if (!localidade) {
                    if (!patient.bairro.id)
                        patient.bairro.id = UUID.randomUUID().toString()
                    patient.bairro.id = patient.bairro.id
                    localidadeService.save(patient.bairro)
                }
            }
            patientService.save(patient)
        } catch (ValidationException e) {
            respond patient.errors
            return
        }

        render JSONSerializer.setJsonObjectResponse(patient) as JSON
    }

    @Transactional
    def delete(Long id) {
        if (id == null || patientService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        List<String> toIncludeProps = new ArrayList<>()
        toIncludeProps.add("identifiers")
        toIncludeProps.add("clinic")
        // patientService.getAllByClinicId(clinicId, offset, max)
        //render JSONSerializer.setLightObjectListJsonResponse(patientService.getAllByClinicId(clinicId, offset, max), toIncludeProps) as JSON
        render JSONSerializer.setObjectListJsonResponse(patientService.getAllByClinicId(clinicId, offset, max)) as JSON
        //respond patientService.getAllByClinicId(clinicId, offset, max)
    }

    def countPatientSearchResult() {

        Patient patient = new Patient()
        def objectJSON = request.JSON
        patient = objectJSON as Patient

        render patientService.countPatientSearchResult(patient)
    }

    def search() {
        Patient patient = new Patient()
        def objectJSON = request.JSON
        patient = objectJSON as Patient


        def limit = objectJSON.limit != null ? objectJSON.limit as int : 10 // Default limit to 10 if not provided
        def offset = objectJSON.offset != null ? objectJSON.offset as int : 0

        def patientList = patientService.search(patient, offset, limit)

        //  def result = JSONSerializer.setLightObjectListJsonResponse(patientList)

        def result = JSONSerializer.setLightObjectListJsonResponse(patientList)
        (result as List).collect { rs ->
            def auxPatient = Patient.get(rs.id)
            if (auxPatient.identifiers.size() > 0)
                rs.put('identifiers', auxPatient.identifiers)
        }

        render result as JSON
    }


    def searchByParam(String searchString, String clinicId) {
        String replacedString = searchString.replace("-", "/");
        List<Patient> patientList = patientService.search(replacedString, clinicId)
        render JSONSerializer.setObjectListJsonResponse(patientList) as JSON
    }

    def getOpenMRSSession(String interoperabilityId, String openmrsBase64) {

        HealthInformationSystem healthInformationSystem = HealthInformationSystem.get(interoperabilityId)
        InteroperabilityType interoperabilityType = InteroperabilityType.findByCode("URL_BASE")
        InteroperabilityAttribute interoperabilityAttribute = InteroperabilityAttribute.findByHealthInformationSystemAndInteroperabilityType(healthInformationSystem, interoperabilityType)

        render RestOpenMRSClient.getResponseOpenMRSClient(openmrsBase64, null, interoperabilityAttribute.value, "session", "GET")

    }

    def getOpenMRSPatient(String interoperabilityId, String nid, String openmrsBase64) {

        HealthInformationSystem healthInformationSystem = HealthInformationSystem.get(interoperabilityId)
        InteroperabilityType interoperabilityType = InteroperabilityType.findByCode("URL_BASE")
        InteroperabilityAttribute interoperabilityAttribute = InteroperabilityAttribute.findByHealthInformationSystemAndInteroperabilityType(healthInformationSystem, interoperabilityType)

        String urlPath = "patient?q=" + nid.replaceAll("-", "/") + "&v=full&limit=100"

        render RestOpenMRSClient.getResponseOpenMRSClient(openmrsBase64, null, interoperabilityAttribute.value, urlPath, "GET")

    }

    def getOpenMRSPatientProgramDetails(String interoperabilityId, String uuid, String openmrsBase64) {

        HealthInformationSystem healthInformationSystem = HealthInformationSystem.get(interoperabilityId)
        InteroperabilityType interoperabilityType = InteroperabilityType.findByCode("URL_BASE")
        InteroperabilityAttribute interoperabilityAttribute = InteroperabilityAttribute.findByHealthInformationSystemAndInteroperabilityType(healthInformationSystem, interoperabilityType)

        String urlPath = "programenrollment?patient=" + uuid + "&v=default"

        render RestOpenMRSClient.getResponseOpenMRSClient(openmrsBase64, null, interoperabilityAttribute.value, urlPath, "GET")

    }

    def updatePatientUUID(String base64) {

        def objectJSON = request.JSON
        def patientFromJSON = (parseTo(objectJSON.toString()) as Map) as Patient

        if (patientFromJSON.his) {
            HealthInformationSystem healthInformationSystem = HealthInformationSystem.get(patientFromJSON.his.id)
            InteroperabilityType interoperabilityType = InteroperabilityType.findByCode("URL_BASE")
            InteroperabilityAttribute interoperabilityAttribute = InteroperabilityAttribute.findByHealthInformationSystemAndInteroperabilityType(healthInformationSystem, interoperabilityType)

            JSONObject responsePost = (JSONObject) RestOpenMRSClient.getResponseOpenMRSClient(base64, null, interoperabilityAttribute.value, "session", "GET")
            if (responsePost == null || responsePost.authenticated == false ||
                    responsePost.authenticated == null) {
                response.status = 400 // Set the HTTP status code to indicate a bad request
                response.setContentType("text/plain")
                response.outputStream << 'O Utilizador não se encontra no OpenMRS ou serviço rest no OpenMRS não se encontra em funcionamento'
            } else {
                String urlPath = "patient/" + patientFromJSON.hisUuid
                List<PatientServiceIdentifier> identifiersList = new ArrayList<>()
                JSONObject responsePostGet = (JSONObject) RestOpenMRSClient.getResponseOpenMRSClient(base64, null, interoperabilityAttribute.value, urlPath, "GET")

                if (responsePostGet != null && responsePostGet.person != null) {
                    Patient existsPatientUUID = Patient.findByHisUuid(patientFromJSON.hisUuid)
                    if (existsPatientUUID == null) {
                        Patient patientToUpdate = Patient.findById(objectJSON.id)
                        patientToUpdate.hisUuid = patientFromJSON.hisUuid
//                        patientToUpdate.clinic = Clinic.findByMainClinic(true)
//                        patientToUpdate.clinic.sectors = [].withDefault {new ClinicSector()}
//                        if (patientToUpdate.identifiers != null) {
//                            patientToUpdate.identifiers = [].withDefault { new PatientServiceIdentifier() }
//                            patientToUpdate.save()
//                            (objectJSON.identifiers as List).collect { item ->
//                                if (item) {
//                                    def identifier = PatientServiceIdentifier.get(item.id)
//                                    identifier.patient = patientToUpdate
//                                    identifier.save()
//                                    identifiersList.add(identifier)
//                                }
//                            }
////                            patientToUpdate.identifiers = identifiersList
//                        }
//
//
//                        patientToUpdate.save()
//                        render JSONSerializer.setJsonObjectResponse(patientToUpdate) as JSON
                        render JSONSerializer.setJsonLightObjectResponse(patientToUpdate) as JSON
                    } else if (existsPatientUUID.id != patientFromJSON.id) {
                        response.status = 400
                        response.setContentType("text/plain")
                        response.outputStream << 'Ja Existe no iDMED um paciente com UUID digitado'
                    }
                    render status: NO_CONTENT
                } else {
                    response.status = 400
                    response.setContentType("text/plain")
                    response.outputStream << 'O paciente com o UUID digitado nao existe no openMRS'
                }
            }
        } else {
            response.status = 400
            response.setContentType("text/plain")
            response.outputStream << 'Paciente criado a partir do iDMED. Este paciente não tem ligação com fonte de dados OpenMRS'
        }
    }

    def getPatientsInClinicSector(String clinicSectorId,int offset , int max) {
        render JSONSerializer.setObjectListJsonResponse(patientService.getAllPatientsInClinicSector(Clinic.findById(clinicSectorId),offset,max)) as JSON
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

    def mergeUnitePatients(String patientToHoldId, String patientToDeleteId) {
        def patientToHold = Patient.findById(patientToHoldId)
        def patientToDelete = Patient.findById(patientToDeleteId)

        def patientServicesToHold = PatientServiceIdentifier.findAllByPatient(patientToHold)
        def patientServiceToDelete = PatientServiceIdentifier.findAllByPatient(patientToDelete)
        def resultMap = new HashMap<String, List<PatientServiceIdentifier>>();
        (patientServicesToHold + patientServiceToDelete).eachWithIndex { patientServiceIdentifier, index ->
            resultMap.computeIfAbsent(patientServiceIdentifier.service.code, { k -> new ArrayList<>() }).add(patientServiceIdentifier);
        }
        println(resultMap)

        resultMap.keySet().each { it ->
            def psis = resultMap.get(it)
            if (psis.size() == 2) {
                psis.eachWithIndex { it2, index ->
                    if (it2.patient.id == patientToDeleteId) {
                        def episodes = Episode.findAllByPatientServiceIdentifier(it2)
                        for (Episode episode : episodes) {
                            episode.setPatientServiceIdentifier(psis[index == 0 ? 1 : 0])
                            Episode.withTransaction {
                                episode.save()
                            }
                        }
                    }
                }
            } else {
                psis.eachWithIndex { it2, index ->
                    if (it2.patient.id == patientToDeleteId) {
                        it2.setPatient(patientToHold)
                        PatientServiceIdentifier.withTransaction {
                            it2.save()
                        }
                    }
                }
            }
        }


        def patientVisits = PatientVisit.findAllByPatient(patientToDelete)
        def patientVisitsPatientToHold = PatientVisit.findAllByPatient(patientToHold)
        def commonVisits = patientVisits.findAll { visit1 ->
            patientVisitsPatientToHold.any { visit2 -> visit1.visitDate == visit2.visitDate }
        }
        println(commonVisits)
        if (commonVisits.size() == 0) {
            for (PatientVisit patientVisit : patientVisits) {
                patientVisit.setPatient(patientToHold)
                PatientVisit.withTransaction {
                    patientVisit.save()
                }
            }
        }
        Patient.withTransaction {
            /*
            def patientToDelete1 = patientServiceToDelete.patient
            patientServiceToDelete.episodes = []
            patientServiceToDelete.delete()
            patientToDelete.delete()
*/
            patientServiceToDelete.each { it5 ->
                it5.episodes = []
                it5.delete()
            }
            patientToDelete.delete()
        }
        render status: NO_CONTENT
/*
        def patientVisits = PatientVisit.findAllByPatient(patientToDelete)
        for (PatientVisit patientVisit : patientVisits) {
            patientVisit.setPatient(patientToHold)
            PatientVisit.withTransaction {
                patientVisit.save()
                println(matchId++)
            }
        }

        for (PatientServiceIdentifier patientService : patientServiceToDelete) {

            def episodes = Episode.findAllByPatientServiceIdentifier(patientService)
            for (Episode episode : episodes) {
                episode.setPatientServiceIdentifier(patientService)
                Episode.withTransaction {
                    episode.save()
                    println(matchIdd++)
                }
            }
        }


         */

    }
}
