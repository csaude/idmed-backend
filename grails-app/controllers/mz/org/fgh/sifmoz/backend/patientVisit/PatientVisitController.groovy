package mz.org.fgh.sifmoz.backend.patientVisit


import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.screening.*
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stock.StockService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.validation.BindingResult

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

//import grails.plugin.json.view.api.JsonView

class PatientVisitController extends RestfulController {

    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPackService packService
    VitalSignsScreeningService vitalSignsScreeningService
    TBScreeningService tbScreeningService
    AdherenceScreeningService adherenceScreeningService
    RAMScreeningService ramScreeningService
    PregnancyScreeningService pregnancyScreeningService
    StockService stockService
    IPatientVisitDetailsService patientVisitDetailsService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientVisitController() {
        super(PatientVisit)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientVisitService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(patientVisitService.get(id)) as JSON
    }

    def saveRecord(PatientVisit visit) {
        render JSONSerializer.setJsonObjectResponse(visit) as JSON
    }

    @Transactional
    def save() {
        PatientVisit visit = new PatientVisit()
        def objectJSON = request.JSON
        Clinic clinic = Clinic.findWhere(mainClinic: true)
        if (!objectJSON?.patientVisitDetails?.isEmpty()) {
            def amtPerTimePackaged = objectJSON?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime + ""
            objectJSON?.patientVisitDetails[0]?.pack?.packagedDrugs[0]?.amtPerTime = amtPerTimePackaged ? Double.parseDouble(amtPerTimePackaged) : 0
            def amtPerTimePrescribed = objectJSON?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime + ""
            objectJSON?.patientVisitDetails[0]?.prescription?.prescribedDrugs[0]?.amtPerTime = amtPerTimePrescribed ? Double.parseDouble(amtPerTimePrescribed) : 0

        }

        def syncStatus = objectJSON["syncStatus"]
        visit = objectJSON as PatientVisit

        visit.beforeInsert()
        visit.validate()

        if (objectJSON.id) {
            visit.id = UUID.fromString(objectJSON.id)

            visit.patientVisitDetails.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.patientVisitDetails[index].id)
                item.prescription.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.id)
                item.prescription.prescribedDrugs.eachWithIndex { item2, index2 ->
                    item2.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescribedDrugs[index2].id)
                    item2.clinic = clinic
                }
                item.prescription.prescriptionDetails.eachWithIndex { item3, index3 ->
                    item3.id = UUID.fromString(objectJSON.patientVisitDetails[index].prescription.prescriptionDetails[index3].id)
                    item3.clinic = clinic
                }
                item.pack.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.id)
                item.pack.packagedDrugs.eachWithIndex { item4, index4 ->
                    item4.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].id)
                    item4.clinic = clinic
                    item4.packagedDrugStocks.eachWithIndex { item5, index5 ->
                        item5.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].packagedDrugStocks[index5].id)
                    }
                }
            }

            visit.adherenceScreenings.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.adherenceScreenings[index].id)
                item.clinic = clinic
            }
            visit.vitalSignsScreenings.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.vitalSignsScreenings[index].id)
                item.clinic = clinic
            }
            visit.pregnancyScreenings.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.pregnancyScreenings[index].id)
                item.clinic = clinic
            }
            visit.tbScreenings.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.tbScreenings[index].id)
                item.clinic = clinic
            }
            visit.ramScreenings.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.ramScreenings[index].id)
                item.clinic = clinic
            }
        }

        if (visit.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond visit.errors
            return
        }

        try {
            PatientVisit existingPatientVisit = PatientVisit.findByVisitDateAndPatient(visit.visitDate, visit.patient)
            if (existingPatientVisit != null) {
                visit.vitalSignsScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.save()
                }
                if (visit.patient.gender.startsWith('F')) {
                    visit.pregnancyScreenings.each { item ->
                        item.visit = existingPatientVisit
                        item.save()
                    }
                    existingPatientVisit.pregnancyScreenings = visit.pregnancyScreenings
                }
                visit.ramScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.save()
                }
                visit.adherenceScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.save()
                }
                visit.tbScreenings.each { item ->
                    item.visit = existingPatientVisit
                    item.save()
                }
                visit.patientVisitDetails.each { item ->
                    item.patientVisit = existingPatientVisit
                    Prescription existingPrescription = Prescription.findById(item.prescription.id)
                    if (existingPrescription == null) {
                        incrementPrescriptionSeq(item.prescription, item.episode)
                        prescriptionService.save(item.prescription)
                    }
                    packService.save(item.pack)
                    reduceStock(item.pack, syncStatus)
                }
                existingPatientVisit.vitalSignsScreenings = visit.vitalSignsScreenings
                existingPatientVisit.ramScreenings = visit.ramScreenings
                existingPatientVisit.adherenceScreenings = visit.adherenceScreenings
                existingPatientVisit.tbScreenings = visit.tbScreenings
                existingPatientVisit.patientVisitDetails = visit.patientVisitDetails

                visit = existingPatientVisit

            } else {
                visit.patientVisitDetails.each { item ->
                    Prescription existingPrescription = Prescription.findById(item.prescription.id)
                    if (existingPrescription != null) {
                        item.prescription = existingPrescription
                    }
                    item.pack.packagedDrugs.each { packagedDrugs ->
                        def clinicalService = item.episode.patientServiceIdentifier.service
                        if (!packagedDrugs.drug.clinicalService) {
                            packagedDrugs.drug.clinicalService = clinicalService
                        }
                    }
                    incrementPrescriptionSeq(item.prescription, item.episode)
                    prescriptionService.save(item.prescription)
                    packService.save(item.pack)
                    reduceStock(item.pack, syncStatus)
                }
            }

            patientVisitService.save(visit)

        } catch (ValidationException e) {
            transactionStatus.setRollbackOnly()
            respond visit.errors
            return
        }

        def patientVisitDetailsJSON = JSONSerializer.setObjectListJsonResponse(visit.patientVisitDetails as List)
        def vitalSignsScreeningsJSON = JSONSerializer.setObjectListJsonResponse(visit.vitalSignsScreenings as List)
        def ramScreeningsJSON = JSONSerializer.setObjectListJsonResponse(visit.ramScreenings as List)
        def adherenceScreeningsJSON = JSONSerializer.setObjectListJsonResponse(visit.adherenceScreenings as List)
        def tbScreeningsJSON = JSONSerializer.setObjectListJsonResponse(visit.tbScreenings as List)
        def pregnancyScreeningsJSON = JSONSerializer.setObjectListJsonResponse(visit.pregnancyScreenings as List)

        def result = JSONSerializer.setJsonObjectResponse(visit)
        if (patientVisitDetailsJSON.length() > 0) {
            result.put('patientVisitDetails', patientVisitDetailsJSON)

        } else
            result.remove('patientVisitDetails')

        if (vitalSignsScreeningsJSON.length() > 0)
            result.put('vitalSignsScreenings', vitalSignsScreeningsJSON)
        else
            result.remove('vitalSignsScreenings')

        if (ramScreeningsJSON.length() > 0)
            result.put('ramScreenings', ramScreeningsJSON)
        else
            result.remove('ramScreenings')

        if (pregnancyScreeningsJSON.length() > 0)
            result.put('pregnancyScreenings', ramScreeningsJSON)
        else
            result.remove('pregnancyScreenings')

        if (adherenceScreeningsJSON.length() > 0)
            result.put('adherenceScreenings', adherenceScreeningsJSON)
        else
            result.remove('adherenceScreenings')

        if (tbScreeningsJSON.length() > 0)
            result.put('tbScreenings', tbScreeningsJSON)
        else
            result.remove('tbScreenings')

        render result as JSON
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        PatientVisit patientVisitDB = PatientVisit.get(objectJSON.id)
        def syncStatus = objectJSON["syncStatus"]
        if (patientVisitDB == null) {
            render status: NOT_FOUND
            return
        }
        //updating db object
        patientVisitDB.properties = objectJSON as BindingResult
        List<AdherenceScreening> adherenceScreenings = new ArrayList<>()
        List<VitalSignsScreening> vitalSignsScreenings = new ArrayList<>()
        List<PregnancyScreening> pregnancyScreenings = new ArrayList<>()
        List<TBScreening> tbScreenings = new ArrayList<>()
        List<RAMScreening> ramScreenings = new ArrayList<>()

        patientVisitDB.adherenceScreenings = [].withDefault { new AdherenceScreening() }
        patientVisitDB.tbScreenings = [].withDefault { new TBScreening() }
        patientVisitDB.vitalSignsScreenings = [].withDefault { new VitalSignsScreening() }
        patientVisitDB.pregnancyScreenings = [].withDefault { new PregnancyScreening() }
        patientVisitDB.ramScreenings = [].withDefault { new RAMScreening() }

        (objectJSON.adherenceScreenings as List).collect { item ->
            if (item) {
                def adherenceScreeningObject = new AdherenceScreening(parseTo(item.toString()) as Map)
                adherenceScreeningObject.id = item.id
                def adherenceScreening = AdherenceScreening.get(adherenceScreeningObject.id)
                if (!adherenceScreening)
                    adherenceScreening = adherenceScreeningObject
                adherenceScreening.hasPatientCameCorrectDate = adherenceScreeningObject.hasPatientCameCorrectDate
                adherenceScreening.daysWithoutMedicine = adherenceScreeningObject.daysWithoutMedicine
                adherenceScreening.patientForgotMedicine = adherenceScreeningObject.patientForgotMedicine
                adherenceScreening.lateDays = adherenceScreeningObject.lateDays
                adherenceScreening.lateMotives = adherenceScreeningObject.lateMotives
                adherenceScreening.visit = patientVisitDB
                adherenceScreenings.add(adherenceScreening)
            }
        }

        (objectJSON.tbScreenings as List).collect { item ->
            if (item) {
                def tbScreeningObject = new TBScreening(parseTo(item.toString()) as Map)
                tbScreeningObject.id = item.id
                def tbScreening = TBScreening.get(tbScreeningObject.id)
                if (!tbScreening)
                    tbScreening = tbScreeningObject
                tbScreening.parentTBTreatment = tbScreeningObject.parentTBTreatment
                tbScreening.cough = tbScreeningObject.cough
                tbScreening.fever = tbScreeningObject.fever
                tbScreening.losingWeight = tbScreeningObject.losingWeight
                tbScreening.treatmentTB = tbScreeningObject.treatmentTB
                tbScreening.treatmentTPI = tbScreeningObject.treatmentTPI
                tbScreening.startTreatmentDate = tbScreeningObject.startTreatmentDate
                tbScreening.fatigueOrTirednessLastTwoWeeks = tbScreeningObject.fatigueOrTirednessLastTwoWeeks
                tbScreening.sweating = tbScreeningObject.sweating
                tbScreening.visit = patientVisitDB
                tbScreenings.add(tbScreening)
            }
        }


        (objectJSON.vitalSignsScreenings as List).collect { item ->
            if (item) {
                def vitalSignsScreeningObject = new VitalSignsScreening(parseTo(item.toString()) as Map)
                vitalSignsScreeningObject.id = item.id
                def vitalSignsScreening = VitalSignsScreening.get(vitalSignsScreeningObject.id)
                if (!vitalSignsScreening)
                    vitalSignsScreening = vitalSignsScreeningObject
                vitalSignsScreening.distort = vitalSignsScreeningObject.distort
                vitalSignsScreening.imc = vitalSignsScreeningObject.imc
                vitalSignsScreening.weight = vitalSignsScreeningObject.weight
                vitalSignsScreening.systole = vitalSignsScreeningObject.systole
                vitalSignsScreening.height = vitalSignsScreeningObject.height
                vitalSignsScreening.visit = patientVisitDB
                vitalSignsScreenings.add(vitalSignsScreening)
            }
        }

        (objectJSON.pregnancyScreenings as List).collect { item ->
            if (item) {
                def pregnancyScreeningObject = new PregnancyScreening(parseTo(item.toString()) as Map)
                pregnancyScreeningObject.id = item.id
                def pregnancyScreening = PregnancyScreening.get(pregnancyScreeningObject.id)
                if (!pregnancyScreening)
                    pregnancyScreening = pregnancyScreeningObject
                pregnancyScreening.pregnant = pregnancyScreeningObject.pregnant
                pregnancyScreening.menstruationLastTwoMonths = pregnancyScreeningObject.menstruationLastTwoMonths
                pregnancyScreening.lastMenstruation = pregnancyScreeningObject.lastMenstruation
                pregnancyScreening.visit = patientVisitDB
                pregnancyScreenings.add(pregnancyScreening)
            }
        }

        (objectJSON.ramScreenings as List).collect { item ->
            if (item) {
                def ramScreeningObject = new RAMScreening(parseTo(item.toString()) as Map)
                ramScreeningObject.id = item.id
                def ramScreening = RAMScreening.get(ramScreeningObject.id)
                if (!ramScreening)
                    ramScreening = ramScreeningObject
                ramScreening.adverseReaction = ramScreeningObject.adverseReaction
                ramScreening.adverseReactionMedicine = ramScreeningObject.adverseReactionMedicine
                ramScreening.visit = patientVisitDB
                ramScreenings.add(ramScreening)
            }
        }

        patientVisitDB.ramScreenings = ramScreenings
        patientVisitDB.vitalSignsScreenings = vitalSignsScreenings
        patientVisitDB.tbScreenings = tbScreenings
        patientVisitDB.adherenceScreenings = adherenceScreenings
        patientVisitDB.pregnancyScreenings = pregnancyScreenings

        //Only updated pack and packagedDrugs (refazer dispensa)
        patientVisitDB.patientVisitDetails.eachWithIndex { item, index ->
            if (item.pack) {
                item.pack.packagedDrugs.eachWithIndex { item4, index4 ->
                    item4.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].id)
//                    item4.drug.stockList = null
                    item4.packagedDrugStocks.eachWithIndex { item5, index5 ->
                        item5.id = UUID.fromString(objectJSON.patientVisitDetails[index].pack.packagedDrugs[index4].packagedDrugStocks[index5].id)
                    }
                }
            }
        }
        if (patientVisitDB.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond visit.errors
            return
        }
//        try {
//            patientVisitDB.patientVisitDetails.each { item ->
//                if (item.pack) {
//                    restoreStock(item.pack, syncStatus)
//                    reduceStock(item.pack, syncStatus)
//                    packService.save(item.pack)
//                }
//            }
//            patientVisitService.save(patientVisitDB)
//        } catch (ValidationException e) {
//            respond patientVisitDB.errors
//            return
//        }

        render JSONSerializer.setJsonObjectResponse(patientVisitDB) as JSON
    }

//    @Transactional(rollbackFor = Throwable)
    @Transactional
    def delete(String id) {

        PatientVisit patientVisitDB = PatientVisit.get(id)

        try {
            patientVisitDB.patientVisitDetails.each { item ->
                def packToRemove = item.pack
                def prescriptionToRemove = null
                if (item.pack) {
                    restoreStock(item.pack, 'N')
                    if (PatientVisitDetails.countByPrescription(item.prescription) == 1)
                        prescriptionToRemove = item.prescription
                }
                item.delete()
                packService.delete(packToRemove.id)
                prescriptionToRemove ? prescriptionService.delete(prescriptionToRemove.id) : ''
            }
        } catch (ValidationException e) {
            respond patientVisitDB.errors
            return
        }
        if (patientVisitService.delete(patientVisitDB.id).hasErrors()) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByClinicId(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitService.getAllByClinicId(clinicId, offset, max)) as JSON
    }

    def getByPatientId(String patientId) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitService.getAllByPatientId(patientId)) as JSON
    }

    def getLastVisitOfPatient(String patientId) {
        render JSONSerializer.setJsonObjectResponse(patientVisitService.getLastVisitOfPatient(patientId)) as JSON
    }

    def getAllLastVisitWithScreeningOfClinic(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitService.getAllLastWithScreening(clinicId, offset, max)) as JSON
    }

    void restoreStock(Pack pack, def syncStatus) {
        if ((pack.syncStatus == 'N' && syncStatus == '') || (pack.syncStatus == 'R' && syncStatus == 'R')) {
            List<PackagedDrug> packagedDrugsDb = PackagedDrug.findAllByPack(pack)
            List<PackagedDrugStock> packagedDrugStocks = PackagedDrugStock.findAllByPackagedDrug(packagedDrugsDb)
            for (PackagedDrugStock packagedDrugStock : packagedDrugStocks) {
                Stock stock = Stock.findById(packagedDrugStock.stock.id)
                stock.stockMoviment += packagedDrugStock.quantitySupplied
                stockService.save(stock)
                packagedDrugStock.delete()
            }
            packagedDrugsDb.each { item ->
                item.delete()
            }
        }
    }

    void reduceStock(Pack pack, def syncStatus) {
        if ((pack.syncStatus == 'N' && syncStatus == '') || (pack.syncStatus == 'R' && syncStatus == 'R')) {
            pack.packagedDrugs.each { pcDrugs ->

                def quantityControl = pcDrugs.quantitySupplied

                while (quantityControl > 0) {
                    Stock stock = Stock.get(getFirstExpiredBatchFromDrug(pcDrugs.drug, pack.pickupDate))

                    if (stock) {
                        if (stock.stockMoviment <= quantityControl && stock.stockMoviment < 0) {
                            quantityControl -= stock.stockMoviment
                            stock.stockMoviment = 0
                        } else {
                            stock.stockMoviment -= quantityControl
                            quantityControl = 0
                        }
                        stock.packagedDrugs = []
                        stock.save(flush: true)
                    }
                }
            }
        }
    }

    void incrementPrescriptionSeq(Prescription newPrescription, Episode episode) {
        def random = new Random()
        def patientVisitDetails = patientVisitDetailsService.getLastByEpisodeId(episode.id)
        def sequence = 0
        def newSequence = 10000 + random.nextInt(900000)
        newPrescription.setPrescriptionSeq(episode.patientServiceIdentifier.value + "-" + newSequence)
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

    private static String getFirstExpiredBatchFromDrug(Drug drug, Date pickUpdate) {

        def stockList = Stock.
                findAllByDrugAndExpireDateGreaterThanEqualsAndExpireDateGreaterThanAndStockMovimentGreaterThan(drug,
                        Utilities.addDaysInDate(pickUpdate, drug.packSize),
                        new Date(), 0,
                        [sort: "expireDate", order: "asc"])
        if (!stockList.isEmpty())
            return stockList.first().id

        return null
    }

    def getAllByVisitIds() {
        def objectJSON = request.JSON
        List<String> ids = objectJSON
        render JSONSerializer.setObjectListJsonResponse(PatientVisit.findAllByIdInList(ids)) as JSON
    }

}
