package mz.org.fgh.sifmoz.backend.patientVisit

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
@CompileStatic
@EnableScheduling
@Transactional
class RestExternalPatientVisitService {

    final String requestMethod_POST = "POST"
    final char syncStatusReady = 'R'.toCharacter()
    final char syncStatusUPDATED = 'U'.toCharacter()

    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPatientVisitDetailsService patientVisitDetailsService
    ExternalPatientVisitService externalPatientVisitService
    IPackService packService
    IEpisodeService episodeService

    static lazyInit = false

//    @Scheduled(cron = "0 0 12 * * 1,5")
    @Scheduled(fixedDelay = 90000L)
    void schedulerRequestRunning() {
        PatientVisit.withTransaction {
            Clinic mainClinic = Clinic.findWhere(mainClinic: true)

            List<ExternalPatientVisit> externalPatientVisitList = ExternalPatientVisit.findAllWhere(targetClinicId:mainClinic.id, syncStatus: syncStatusReady)

            externalPatientVisitList.each { externalPatientVisit ->
                PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findWhere(value: externalPatientVisit.nid)
                def lastEpisode = episodeService.getLastWithVisitByIndentifier(patientServiceIdentifier, mainClinic)

                if(patientServiceIdentifier){
                    savePatientVisit(externalPatientVisit, patientServiceIdentifier, lastEpisode)
                }
            }

        }
    }

    @Transactional
    void savePatientVisit(ExternalPatientVisit externalPatientVisit, PatientServiceIdentifier patientServiceIdentifier, Episode lastEpisode) {
        PatientVisit visit = new PatientVisit(parseTo(externalPatientVisit.jsonObject) as Map)

        if (!visit?.patientVisitDetails?.isEmpty()) {
            def amtPerTimePackaged = visit?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime + ""
            visit?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime = amtPerTimePackaged ? Double.parseDouble(amtPerTimePackaged) : 0
            def amtPerTimePrescribed = visit?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime + ""
            visit?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime = amtPerTimePrescribed ? Double.parseDouble(amtPerTimePrescribed) : 0
        }

        visit.beforeInsert()
        visit.validate()
        visit.id = UUID.fromString(externalPatientVisit.id)
        visit.origin = externalPatientVisit.targetClinicId
        visit.clinic = patientServiceIdentifier.clinic

        visit.patientVisitDetails.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.patientVisitDetails[index].id)
            item.origin = visit.origin
//            item.prescription.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.id)
            Prescription prescriptionCheck = Prescription.findWhere(id:  item.prescription.id)

            if (prescriptionCheck)
                item.prescription.origin = prescriptionCheck.origin

            item.prescription.prescribedDrugs.eachWithIndex { item2, index2 ->
//                item2.beforeInsert()
//                item2.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescribedDrugs[index2].id)
                if (prescriptionCheck) {
                    item2.origin = prescriptionCheck.origin
                    item2.clinic = prescriptionCheck.clinic
                } else {
                    item2.origin = visit.origin
                    item2.clinic = visit.clinic
                }
            }

            item.prescription.prescriptionDetails.eachWithIndex { item3, index3 ->
//                item3.beforeInsert()
//                item3.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescriptionDetails[index3].id)
                if (prescriptionCheck) {
                    item3.origin = prescriptionCheck.origin
                    item3.clinic = prescriptionCheck.clinic
                } else {
                    item3.origin = visit.origin
                    item3.clinic = visit.clinic
                }
            }

//            item.pack.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.id)
            item.episode = lastEpisode
            item.pack.origin = visit.origin
            item.pack.packagedDrugs.eachWithIndex { item4, index4 ->
//                item4.beforeInsert()
//                item4.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].id)
                item4.clinic = visit.clinic
                item4.origin = visit.origin
            }
        }

        visit.adherenceScreenings.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.adherenceScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.vitalSignsScreenings.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.vitalSignsScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.pregnancyScreenings.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.pregnancyScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.tbScreenings.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.tbScreenings[index].id)
            item.origin = visit.origin
            item.clinic = visit.clinic
        }
        visit.ramScreenings.eachWithIndex { item, index ->
//            item.beforeInsert()
//            item.id = UUID.fromString(objectJSON.ramScreenings[index].id)
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
                    Prescription existingPrescription = Prescription.findWhere(id:  item.prescription.id)
                    if (existingPrescription == null) {
                        //  item.prescription.origin = existingPatientVisit.origin
                        incrementPrescriptionSeq(item.prescription, item.episode)
                        prescriptionService.save(item.prescription)
                    }
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
                    item.episode = lastEpisode
                    item.pack.origin = visit.origin
                    //  item.episode.origin = visit.origin
                    Prescription existingPrescription = Prescription.findWhere(id:  item.prescription.id)
                    if (existingPrescription != null) {
                        item.prescription = existingPrescription
                        // item.prescription.origin = existingPrescription.origin
                    }
                    item.pack.packagedDrugs.each { packagedDrugs ->
                        def clinicalService = item.episode.patientServiceIdentifier.service
                        if (!packagedDrugs.drug.clinical_service_id) {
                            packagedDrugs.origin = item.pack.origin
                            packagedDrugs.drug.clinical_service_id = clinicalService.id
                        }
                    }
                    incrementPrescriptionSeq(item.prescription, item.episode)
                    prescriptionService.save(item.prescription)
                    packService.save(item.pack)
                }
            }
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
}
