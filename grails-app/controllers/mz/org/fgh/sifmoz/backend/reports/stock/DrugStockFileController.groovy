package mz.org.fgh.sifmoz.backend.reports.stock


import grails.rest.*
import grails.converters.*

class DrugStockFileController {

    DrugStockFileService drugStockFileService

	static responseFormats = ['json', 'xml']
	
    def index() { }

    def getDrugSumaryEvents(String clinicId, String drugId) {
        respond drugStockFileService.getDrugSumaryEvents(clinicId, drugId)
    }

    def getDrugBatchSumaryEvents(String clinicId, String stockId) {
        respond drugStockFileService.getDrugBatchSumaryEvents(clinicId, stockId)
    }

    def getDrugSumaryEventsMobile(String clinicId, String drugId){
        respond drugStockFileService.getDrugSumaryEventsMobile(clinicId,drugId)
    }

    def getDrugBatchSumaryEventsMobile(String clinicId, String stockId) {
        respond drugStockFileService.getDrugBatchSumaryEventsMobile(clinicId, stockId)
    }

    def getDrugFileMobile(String clinicId) {
        respond drugStockFileService.getDrugFileMobile(clinicId)

    }
}
