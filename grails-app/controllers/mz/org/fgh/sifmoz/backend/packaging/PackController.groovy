package mz.org.fgh.sifmoz.backend.packaging

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugService
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStockService
import mz.org.fgh.sifmoz.backend.clinic.ClinicService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stock.StockService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PackController extends RestfulController {

    IPackService packService
    StockService stockService
    PackagedDrugStockService packagedDrugStockService
    PackagedDrugService packagedDrugService
    ClinicService clinicService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PackController() {
        super(Pack)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(packService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(packService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Pack pack = new Pack()
        def objectJSON = request.JSON
        pack = objectJSON as Pack

        pack.beforeInsert()
        pack.validate()

        if (objectJSON.id) {
            pack.id = UUID.fromString(objectJSON.id)
            pack.packagedDrugs.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.packagedDrugs[index].id)
                item.drug.stockList = null
                item.packagedDrugStocks.eachWithIndex { item2, index2 ->
                    item2.id = UUID.fromString(objectJSON.packagedDrugs[index].packagedDrugStocks[index2].id)
                }
            }
        }

        if (pack.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond pack.errors
            return
        }

        try {
            configPackOrigin(pack)
            packService.save(pack)
        } catch (ValidationException e) {
            respond pack.errors
            return
        }

        respond pack, [status: CREATED, view: "show"]
    }

    @Transactional
    def update(Pack pack) {
        if (pack == null) {
            render status: NOT_FOUND
            return
        }
        if (pack.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond pack.errors
            return
        }

        try {

            for (PackagedDrug packagedDrug : pack.packagedDrugs) {
                List<PackagedDrugStock> packagedDrugStocks = PackagedDrugStock.findAllByPackagedDrug(packagedDrug)
                for (PackagedDrugStock packagedDrugStock : packagedDrugStocks) {
                    Stock stock = Stock.findById(packagedDrugStock.stock.id)
                    stock.stockMoviment = packagedDrugStock.quantitySupplied + stock.stockMoviment
                    stockService.save(stock)
                    packagedDrugStock.delete()
                }
                packagedDrug.delete()
            }
            configPackOrigin(pack)
            packService.save(pack)
        } catch (ValidationException e) {
            respond pack.errors
            return
        }

        respond pack, [status: OK, view: "show"]
    }

    @Transactional
    def delete(String id) {
        if (id == null || packService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getAllByClinicId(String clinicId, int offset, int max) {
        Clinic clinic = clinicService.get(clinicId)
        render JSONSerializer.setObjectListJsonResponse(Pack.findAllByClinic(clinic, offset, max)) as JSON
    }

    def getAllLastPackOfClinic(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(packService.getAllLastPackOfClinic(clinicId, offset, max)) as JSON
    }

    // Futuramente reduzir para 2 ultimas prescricoes
    def getLastPackByPatientId(String patientId) {

        def patient = Patient.get(patientId)
        List<Pack> packsList = new ArrayList<Pack>()

        if (patient) {
            def patientServiceIdentifierList = PatientServiceIdentifier.createCriteria().list {
                eq("patient", patient)
            }

            patientServiceIdentifierList.each { patientServiceIdentifier ->
                def episode = Episode.createCriteria().get {
                    eq("patientServiceIdentifier", patientServiceIdentifier)
                    maxResults(1)
                    order("episodeDate", "desc")
                }

                if (episode) {
                    def patientVisitDetails = PatientVisitDetails.createCriteria().list {
                        eq("episode", episode)
                    }

                    if (!patientVisitDetails.isEmpty()) {

                        def prescriptionIds = patientVisitDetails.collect { it.prescription.id }
                        def prescription = Prescription.createCriteria().get {
                            'in'("id", prescriptionIds)
                            maxResults(1)
                            order("prescriptionDate", "desc")
                        }

                        if(prescription){
                            def pdvList = PatientVisitDetails.findAllByPrescription(prescription as Prescription)
                            packsList.addAll(pdvList.pack as List<Pack>)
                        }
                    }
                }
            }
        }

        render JSONSerializer.setObjectListJsonResponse(packsList) as JSON
    }


    def getAllPackByPatientId(String patientId, String serviceCode) {

        def patient = Patient.get(patientId)
        def service = ClinicalService.findByCode(serviceCode)
        def patientServiceIdentifier = PatientServiceIdentifier.findAllByPatientAndService(patient, service)
        List<Pack> packsList = new ArrayList<Pack>()

        if (patient) {
            def patientVisitDetails = PatientVisitDetails.findAllByPatientVisitInListAndEpisodeInList( PatientVisit.findAllByPatient(patient), patientServiceIdentifier.episodes)
            packsList.addAll(patientVisitDetails.pack.toList().sort {it.packDate}.reverse())
        }

        render JSONSerializer.setObjectListJsonResponseLevel3(packsList) as JSON
    }
    def getAllByPackIds() {
        def objectJSON = request.JSON
        List<String> ids = objectJSON
        render JSONSerializer.setObjectListJsonResponse(Pack.findAllByIdInList(ids)) as JSON
    }

    private static Pack configPackOrigin(Pack pack){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL") && checkHasNotOrigin(pack)){
            pack.origin = systemConfigs.description
        }

        return pack
    }

    private static boolean checkHasNotOrigin(Pack pack){
        return pack.origin == null || pack?.origin?.isEmpty()
    }

}
