package mz.org.fgh.sifmoz.backend.patientVisitDetails

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.IPatientVisitService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

//import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer2

import javax.transaction.TransactionalException

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class PatientVisitDetailsController extends RestfulController {

    IPatientVisitDetailsService patientVisitDetailsService
    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPackService packService
    IStockService stockService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    PatientVisitDetailsController() {
        super(PatientVisitDetails)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(patientVisitDetailsService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(patientVisitDetailsService.get(id)) as JSON
    }

    @Transactional
    def save() {
        PatientVisitDetails patientVisitDetails = new PatientVisitDetails()
        def objectJSON = request.JSON
        patientVisitDetails = objectJSON as PatientVisitDetails

        patientVisitDetails.beforeInsert()
        patientVisitDetails.validate()

        if (objectJSON.id) {
            patientVisitDetails.id = UUID.fromString(objectJSON.id)
        }
        if (patientVisitDetails.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientVisitDetails.errors
            return
        }

        try {
            determinePrescriptionPatientType(patientVisitDetails)
            patientVisitDetailsService.save(patientVisitDetails)
        } catch (ValidationException e) {
            respond patientVisitDetails.errors
            return
        }

        respond patientVisitDetails, [status: CREATED, view: "show"]
    }

    @Transactional
    def update(PatientVisitDetails patientVisitDetails) {
        if (patientVisitDetails == null) {
            render status: NOT_FOUND
            return
        }
        if (patientVisitDetails.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond patientVisitDetails.errors
            return
        }

        try {
            patientVisitDetailsService.save(patientVisitDetails)
        } catch (ValidationException e) {
            respond patientVisitDetails.errors
            return
        }

        respond patientVisitDetails, [status: OK, view: "show"]
    }

    @Transactional(rollbackFor = Throwable)
    def delete(String id) {
        PatientVisitDetails patientVisitDetail = PatientVisitDetails.findById(id)
        def patientVisit = PatientVisit.get(patientVisitDetail.patientVisit.id)
        if (patientVisitDetail == null) {
            render status: NOT_FOUND
            return
        }
        try {
            patientVisitDetailsService.delete(id)
            restoreStock(patientVisitDetail.pack)
            packService.delete(patientVisitDetail.pack.id)
            updateErrorLog(id)
            if (PatientVisitDetails.countByPrescription(patientVisitDetail.prescription) == 1)
                prescriptionService.delete(patientVisitDetail.prescription.id)
            render status: NO_CONTENT
            return
        } catch (TransactionalException e) {
            e.printStackTrace()
            render status: NOT_FOUND
            return
        }
    }

    def getAllLastVisitOfClinic(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitDetailsService.getAllLastVisitOfClinic(clinicId, offset, max)) as JSON
    }

    def getAllByClinicId(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitDetailsService.getAllByClinicId(clinicId, offset, max)) as JSON
    }

    def getAllByEpisodeId(String episodeId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(PatientVisitDetails.findAllByEpisode(Episode.get(episodeId))) as JSON
    }

    def getLastByPatientId(String patientId) {

        def patient = Patient.get(patientId)
        def patientServiceIdentifierList = PatientServiceIdentifier.createCriteria().list {
            eq('patient', patient)
        }

        def patientVisitDetailsList = new ArrayList<PatientVisitDetails>()
        patientServiceIdentifierList.each { patientServiceIdentifier ->

            def episodeList = Episode.createCriteria().list {
                eq('patientServiceIdentifier', patientServiceIdentifier)
                maxResults(3)
                order("episodeDate", "desc")
            }

            if (!episodeList.isEmpty()) {

                episodeList.each { episode ->
                    def patientVisitDetails = PatientVisitDetails.createCriteria().list {
                        eq('episode', episode)
                    }

                    if(!patientVisitDetails.isEmpty()){
                        def prescriptionIds = patientVisitDetails.collect { it.prescription.id }
                        def prescription = Prescription.createCriteria().get {
                            'in'("id", prescriptionIds)
                            maxResults(1)
                            order("prescriptionDate", "desc")
                        }

                        if(prescription){
                            patientVisitDetails.each { it ->
                                if (it.prescription.id == prescription.id)
                                    patientVisitDetailsList.add(it as PatientVisitDetails)
                            }
                        }
                    }
                }
            }
        }

        render JSONSerializer.setObjectListJsonResponseLevel3(patientVisitDetailsList) as JSON
    }

    def getAllofPrecription(String prescriptionId) {
        render JSONSerializer.setObjectListJsonResponse(PatientVisitDetails.findAllByPrescription(Prescription.findById(prescriptionId))) as JSON
    }

    def getLastByEpisodeId(String episodeId) {
        PatientVisitDetails pvd = patientVisitDetailsService.getLastByEpisodeId(episodeId)
        if (pvd != null) {
            render JSONSerializer.setJsonObjectResponse(pvd) as JSON
        } else render status: NO_CONTENT
    }

    void determinePrescriptionPatientType(PatientVisitDetails patientVisitDetails) {
        PatientVisit patientVisit = patientVisitService.getLastVisitOfPatient(patientVisitDetails.getPatientVisit().getPatient().id)

        if (patientVisitDetails.episode.startStopReason.isManutencao() || patientVisitDetails.episode.startStopReason.isTransferido()) {
            patientVisitDetails.prescription.setPatientType(patientVisitDetails.episode.startStopReason.code)
        } else if (patientVisitDetails.prescription.patientType == "Alterar") {
            patientVisitDetails.prescription.setPatientType(Prescription.PATIENT_TYPE_ALTERACAO)
        } else if (patientVisitDetails.episode.startStopReason.isNew() && patientVisit == null) {
            patientVisitDetails.prescription.setPatientType(Prescription.PATIENT_TYPE_NOVO)
        } else if (patientVisitDetails.episode.startStopReason.isNew() && patientVisit != null) {
            patientVisitDetails.prescription.setPatientType(Prescription.PATIENT_TYPE_MANUTENCAO)
        } else {
            patientVisitDetails.prescription.setPatientType(Prescription.PATIENT_TYPE_MANUTENCAO)
        }
        prescriptionService.save(patientVisitDetails.prescription)
    }

    def mySave(PatientVisitDetails patientVisit) {
        this.save(patientVisit)
        render patientVisit
    }

    void restoreStock(Pack pack) {
        if (pack.syncStatus == 'N') {
            for (PackagedDrug packagedDrug : pack.packagedDrugs) {
                List<PackagedDrugStock> packagedDrugStocks = PackagedDrugStock.findAllByPackagedDrug(packagedDrug)
                for (PackagedDrugStock packagedDrugStock : packagedDrugStocks) {
                    Stock stock = Stock.findById(packagedDrugStock.stock.id)
                    stock.stockMoviment = packagedDrugStock.quantitySupplied + stock.stockMoviment
                    stockService.save(stock)
                }
            }
        }
    }

    def updateErrorLog(String patientVisitDetails) {
        OpenmrsErrorLog patientVisitDetailsInLog = OpenmrsErrorLog.findByPatientVisitDetails(patientVisitDetails)
        if (patientVisitDetailsInLog) {
            patientVisitDetailsInLog.returnPickupDate = new Date()
            patientVisitDetailsInLog.save()
        }
    }

    def getAllByPatientId(String patientId) {
        render JSONSerializer.setObjectListJsonResponse(patientVisitDetailsService.getAllByPatientId(patientId)) as JSON
    }

    def getAllByListPatientId() {

        def objectJSON = request.JSON
        List<String> ids = objectJSON.ids
        def clinicSector = objectJSON.clinicSector

        ClinicSector clinicSector2 = ClinicSector.get(clinicSector.id)
        def result = patientVisitDetailsService.getAllByListPatientId(ids, clinicSector2)

        render JSONSerializer.setObjectListJsonResponse(result) as JSON
    }

    def getLastAllByListPatientId() {

        def objectJSON = request.JSON
        List<String> ids = objectJSON.ids

        def result = patientVisitDetailsService.getLastAllByListPatientId(ids)

        render JSONSerializer.setObjectListJsonResponse(result) as JSON
    }
}
