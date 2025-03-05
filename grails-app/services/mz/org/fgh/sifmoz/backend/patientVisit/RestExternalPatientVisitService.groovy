package mz.org.fgh.sifmoz.backend.patientVisit

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.IdmedAuthenticationUtils
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
//@CompileStatic
@EnableScheduling
@Transactional
class RestExternalPatientVisitService {

    final String requestMethod_POST = "POST"
    final char syncStatusReady = 'R'.toCharacter()
    final char syncStatusUPDATED = 'U'.toCharacter()
    final char syncStatusSent = "S".toCharacter()

    IdmedAuthenticationUtils authenticationIdmedUtils = new IdmedAuthenticationUtils()
    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPatientVisitDetailsService patientVisitDetailsService
    ExternalPatientVisitService externalPatientVisitService
    IPackService packService
    IEpisodeService episodeService

    static lazyInit = false

    @Scheduled(fixedDelay = 120000L)
    void schedulerRequestRunning() {
        println  " - REST EXTERNAL PATIENT VISIT FROM PROVINCIAL TO IDMED " + new Date()
        PatientVisit.withTransaction {
            def uuidProvincial = configProvincialUUID()

            List<ExternalPatientVisit> externalPatientVisitList = ExternalPatientVisit.findAllWhere(targetProvinceId: uuidProvincial, syncStatus: syncStatusReady)

            externalPatientVisitList.each { externalPatientVisit ->
                PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findWhere(value: externalPatientVisit.nid)
                Episode lastEpisode = episodeService.getLastWithVisitByIndentifier(patientServiceIdentifier, patientServiceIdentifier?.clinic)

                EpisodeType episodeType = EpisodeType.get(lastEpisode.episodeType.id)
                Clinic clinic = Clinic.get(lastEpisode.clinic.id)
                ClinicSector clinicSector = ClinicSector.get(lastEpisode.clinicSector.id)

                lastEpisode.episodeType = episodeType
                lastEpisode.clinicSector = clinicSector
                lastEpisode.clinic = clinic

                if(patientServiceIdentifier){
                    savePatientVisit(externalPatientVisit, patientServiceIdentifier, lastEpisode)
                }
            }
        }
    }

    @Transactional
    void savePatientVisit(ExternalPatientVisit externalPatientVisit, PatientServiceIdentifier patientServiceIdentifier, Episode lastEpisode) {
        PatientVisit visit = new PatientVisit(parseTo(externalPatientVisit.jsonObject) as Map)
                     visit.patient = patientServiceIdentifier.patient
        def objectJSON = new JsonSlurper().parseText(externalPatientVisit.jsonObject)

        if (!visit?.patientVisitDetails?.isEmpty()) {
            def amtPerTimePackaged = visit?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime + ""
            visit?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime = amtPerTimePackaged ? Double.parseDouble(amtPerTimePackaged) : 0
            def amtPerTimePrescribed = visit?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime + ""
            visit?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime = amtPerTimePrescribed ? Double.parseDouble(amtPerTimePrescribed) : 0
        }

        visit.beforeInsert()
        visit.id = UUID.fromString(externalPatientVisit.id)
        visit.origin = externalPatientVisit.sourceClinicId
        visit.clinic = patientServiceIdentifier.clinic

        visit.patientVisitDetails.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.patientVisitDetails[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
            item.episode = lastEpisode
            item.patientVisit = visit
            item.prescription.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.id)
            Prescription prescriptionCheck = Prescription.findWhere(id:  item.prescription.id)

            if (prescriptionCheck){
                item.prescription.origin = prescriptionCheck.origin
            }else{
                item.prescription.origin = visit.origin
                item.prescription.clinic = visit.clinic
            }

            item.prescription.prescribedDrugs.eachWithIndex { item2, index2 ->
                item2.beforeInsert()
                item2.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescribedDrugs[index2].id)
                if (prescriptionCheck) {
                    item2.origin = visit.origin
                    item2.clinic = prescriptionCheck.clinic
                } else {
                    item2.origin = visit.origin
                    item2.clinic = visit.clinic
                }
            }

            item.prescription.prescriptionDetails.eachWithIndex { item3, index3 ->
                item3.beforeInsert()
                item3.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescriptionDetails[index3].id)
                if (prescriptionCheck) {
                    item3.origin = visit.origin
                    item3.clinic = prescriptionCheck.clinic
                } else {
                    item3.origin = visit.origin
                    item3.clinic = visit.clinic
                }
            }

            item.pack.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.id)
            item.episode = lastEpisode
            item.pack.origin = visit.origin
            item.clinic = visit.clinic
            item.pack.packagedDrugs.eachWithIndex { item4, index4 ->
                item4.beforeInsert()
                item4.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].id)
                item4.clinic = visit.clinic
                item4.origin = visit.origin
            }
        }

        visit.adherenceScreenings.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.adherenceScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.vitalSignsScreenings.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.vitalSignsScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.pregnancyScreenings.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.pregnancyScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.tbScreenings.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.tbScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.ramScreenings.eachWithIndex { item, index ->
            item.beforeInsert()
            item.id = UUID.fromString(objectJSON.ramScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }

        try {
            PatientVisit existingPatientVisit = PatientVisit.findWhere(visitDate:  visit.visitDate, patient:  visit.patient)
            if (existingPatientVisit != null) {
                visit.vitalSignsScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.origin = existingPatientVisit.origin
                    item.clinic = existingPatientVisit.clinic
                    item.save()
                }
                if (visit.patient.gender.startsWith('F')) {
                    visit.pregnancyScreenings.each { item ->
                        item.visit = existingPatientVisit
                        item.origin = existingPatientVisit.origin
                        item.clinic = existingPatientVisit.clinic
                        item.save()
                    }
                    existingPatientVisit.pregnancyScreenings = visit.pregnancyScreenings
                }
                visit.ramScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.origin = existingPatientVisit.origin
                    item.clinic = existingPatientVisit.clinic
                    item.save()
                }
                visit.adherenceScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.origin = existingPatientVisit.origin
                    item.clinic = existingPatientVisit.clinic
                    item.save()
                }
                visit.tbScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.origin = existingPatientVisit.origin
                    item.clinic = existingPatientVisit.clinic
                    item.save()
                }
                visit.patientVisitDetails.each { item ->
                    item.patientVisit = existingPatientVisit
                    item.episode = lastEpisode
                    item.origin = existingPatientVisit.origin
                    item.clinic = existingPatientVisit.clinic
                    Prescription existingPrescription = Prescription.findWhere(id:  item.prescription.id)
                    if (existingPrescription == null) {
                        item.prescription.origin = existingPatientVisit.origin
                        item.prescription.clinic = existingPatientVisit.clinic
                        incrementPrescriptionSeq(item.prescription, item.episode)
                        prescriptionService.save(item.prescription)
                    }
                    item.pack.origin = existingPatientVisit.origin
                    item.pack.clinic = existingPatientVisit.clinic
                    packService.save(item.pack)
                }
                existingPatientVisit.vitalSignsScreenings = visit.vitalSignsScreenings
                existingPatientVisit.ramScreenings = visit.ramScreenings
                existingPatientVisit.adherenceScreenings = visit.adherenceScreenings
                existingPatientVisit.tbScreenings = visit.tbScreenings
                existingPatientVisit.patientVisitDetails = visit.patientVisitDetails
                visit = existingPatientVisit
            } else {
                visit.patientVisitDetails.each { item ->
                    item.patientVisit = visit
                    item.episode = lastEpisode
                    item.pack.origin = visit.origin
                    item.pack.clinic = visit.clinic

                    Prescription existingPrescription = Prescription.findWhere(id:  item.prescription.id)
                    if (existingPrescription != null) {
                        item.prescription = existingPrescription
                        //
                    }else{
                        Doctor doctor = Doctor.findWhere(id: '3F2D1A4B-9C6E-4F89-B5D3-8A2E7F1D0CBA')
                        item.prescription.doctor = doctor
                        item.prescription.origin = visit.origin
                        item.prescription.clinic = visit.clinic
                    }
                    item.pack.packagedDrugs.each { packagedDrugs ->
                        def clinicalService = item.episode.patientServiceIdentifier.service
                        if (!packagedDrugs.drug.clinical_service_id) {
                            packagedDrugs.origin = item.pack.origin
                            packagedDrugs.clinic = item.pack.clinic
                            packagedDrugs.drug.clinical_service_id = clinicalService.id
                        }
                    }
                    incrementPrescriptionSeq(item.prescription, item.episode)
                    prescriptionService.save(item.prescription)
                    packService.save(item.pack)
                }
            }
            visit.validate()
            if(patientVisitService.save(visit)){
                externalPatientVisit.syncStatus = syncStatusUPDATED
                externalPatientVisitService.save(externalPatientVisit)
            }
        } catch (ValidationException e) {
            return
        }
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

    void incrementPrescriptionSeq(Prescription newPrescription, Episode episode) {
        def random = new Random()
        def patientVisitDetails = patientVisitDetailsService.getLastByEpisodeId(episode.id)
        def sequence = 0
        def newSequence = 10000 + random.nextInt(900000)
        newPrescription.setPrescriptionSeq(episode.patientServiceIdentifier.value + "-" + newSequence)
    }

    @Scheduled(fixedDelay = 60000L)
    void dispensesFromUSToProvinceRunning() {
        println  " - REST EXTERNAL PATIENT VISIT FROM IDMED TO PROVINCIAL " + new Date()
        def uuidProvincial = configProvincialUUID()
        PatientVisit.withTransaction {
            List<ExternalPatientVisit> externalPatientVisitList = ExternalPatientVisit.findAllWhere(sourceProvinceId: uuidProvincial, syncStatus: syncStatusReady)

            externalPatientVisitList.each { externalPatientVisit ->
                try {
                    if (!uuidProvincial.equalsIgnoreCase(externalPatientVisit.targetProvinceId)) {
                        Province province = Province.findWhere(id: externalPatientVisit.targetProvinceId)
                        ProvincialServer provincialServer = ProvincialServer.findWhere(code: province.code, destination: "IDMED")

                        def jsonBuilder = new JsonBuilder(externalPatientVisit.properties as Map)

                        // Envia a visita do paciente para o respectivo Servidor provincial
                        def resultRequest = authenticationIdmedUtils.syncExternalPatientVisit(provincialServer, jsonBuilder)

                        if (resultRequest == HttpStatus.CREATED.value() || resultRequest == HttpStatus.CONFLICT.value()) {
                            externalPatientVisit.syncStatus = 'S'.toCharacter()
                            externalPatientVisit.save(flush: true)
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync external patient visit: ${externalPatientVisit.id}", e)
                    e.printStackTrace()
                }
            }
        }
    }

    private static String configProvincialUUID() {
        SystemConfigs systemConfigs = SystemConfigs.findWhere(key: "INSTALATION_TYPE")
        if (systemConfigs && systemConfigs?.value?.equalsIgnoreCase("PROVINCIAL")) {
            Province province = Province.findWhere(code: systemConfigs?.description)
            return province?.id
        }
        return null
    }
}
