package mz.org.fgh.sifmoz.backend.group

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientVisit.IPatientVisitService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class GroupPackHeaderController extends RestfulController{

    IGroupPackHeaderService groupPackHeaderService
    IPackService packService
    IStockService stockService
    IPatientVisitDetailsService patientVisitDetailsService
    IPatientVisitService patientVisitService
    GroupPackService groupPackService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    GroupPackHeaderController () {
        super(GroupPackHeader)
    }
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
       // respond groupPackHeaderService.list(params), model:[groupPackHeaderCount: groupPackHeaderService.count()]
        render JSONSerializer.setObjectListJsonResponse(groupPackHeaderService.list(params)) as JSON
    }

    def show(String id) {
      //  respond groupPackHeaderService.get(id)
        render JSONSerializer.setJsonObjectResponse(groupPackHeaderService.get(id)) as JSON
    }

    @Transactional
    def save() {

        GroupPackHeader groupPackHeader = new GroupPackHeader()
        def objectJSON = request.JSON
        groupPackHeader = objectJSON as GroupPackHeader

        groupPackHeader.beforeInsert()
        groupPackHeader.validate()

        if(objectJSON.id) {
            groupPackHeader.id = UUID.fromString(objectJSON.id)
            groupPackHeader.groupPacks.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.groupPacks[index].id)
                item.patientVisit = objectJSON.groupPacks[index].patientVisit as PatientVisit
                item.pack.id = UUID.fromString(objectJSON.groupPacks[index].pack.id)
                // item.patientVisit.patientVisitDetails[0].pack.id = UUID.fromString(objectJSON.groupPacks[index].patientVisit.patientVisitDetails[0].pack.id)
                item.pack.packagedDrugs.eachWithIndex { item4, index4 ->
                    item4.id = UUID.fromString(objectJSON.groupPacks[index].patientVisit.patientVisitDetails[0].pack.packagedDrugs[index4].id)
                    item4.drug.stockList = null
                    item4.packagedDrugStocks.eachWithIndex { item5, index5 ->
                        item5.id = UUID.fromString(objectJSON.groupPacks[index].patientVisit.patientVisitDetails[0].pack.packagedDrugs[index4].packagedDrugStocks[index5].id)
                    }
                }
                item.patientVisit.id = UUID.fromString(objectJSON.groupPacks[index].patientVisit.id)
                item.patientVisit.patientVisitDetails[0].id = UUID.fromString(objectJSON.groupPacks[index].patientVisit.patientVisitDetails[0].id)
                item.patientVisit.patientVisitDetails[0].pack = item.pack

            }
        }

        if (groupPackHeader.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupPackHeader.errors
            return
        }

        try {
            groupPackHeader.groupPacks.each {item ->
                packService.save(item.pack)
                patientVisitService.save(item.patientVisit)
                reduceStock(item.pack, item.pack.syncStatus)
                //  patientVisitController.save(item.patientVisit)
            }


            groupPackHeaderService.save(groupPackHeader)
        } catch (ValidationException e) {
            respond groupPackHeader.errors
            return
        }

        respond groupPackHeader, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(GroupPackHeader groupPackHeader) {
        if (groupPackHeader == null) {
            render status: NOT_FOUND
            return
        }
        if (groupPackHeader.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond groupPackHeader.errors
            return
        }

        try {
            groupPackHeaderService.save(groupPackHeader)
        } catch (ValidationException e) {
            respond groupPackHeader.errors
            return
        }

        respond groupPackHeader, [status: OK, view:"show"]
    }

    @Transactional
    def delete(String id) {
        GroupPackHeader groupPackHeader = GroupPackHeader.findById(id)
        List<GroupPack> groupPacks = GroupPack.findAllByHeader(groupPackHeader)
        groupPacks.each {item ->
            PatientVisitDetails patientVisitDetail = PatientVisitDetails.findByPack(item.pack)
            PatientVisit patientVisit = PatientVisit.findById(patientVisitDetail.patientVisit.id)
            restoreStock(item.pack)
            patientVisitDetailsService.delete(patientVisitDetail.id)
            patientVisitService.delete(patientVisit.id)
            packService.delete(item.pack.id)
            groupPackService.delete(item.id)
        }
        if (id == null || groupPackHeaderService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    void restoreStock(Pack pack) {
        if(pack.syncStatus == 'N') {
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

    void reduceStock(Pack pack, def syncStatus) {
        if (pack.syncStatus == 'N' && syncStatus == '') {
            pack.packagedDrugs.each { pcDrugs ->
                pcDrugs.packagedDrugStocks.each { pcdStock ->
                    Stock stock = Stock.get(pcdStock.stock.id)
                    stock.packagedDrugs = []
                    stock.stockMoviment -= pcdStock.quantitySupplied
                    stockService.save(stock)
                }
            }
        }
    }
}
