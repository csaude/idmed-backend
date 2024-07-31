package mz.org.fgh.sifmoz.backend.reports.dashboard


import grails.rest.*
import grails.converters.*
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.DashBoardService
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.dashboard.StockAlert

class DashBoardController {

    DashBoardService dashBoardService

	static responseFormats = ['json', 'xml']
    def index() { }

    def getRegisteredPatientByDispenseType (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getRegisteredPatientByDispenseType(reportSearchParams.getStartDate(), reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getPatientsFirstDispenseByGender (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getPatientsFirstDispenseByGender(reportSearchParams.getStartDate(), reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getPatientsFirstDispenseByAge (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getPatientsFirstDispenseByAge(reportSearchParams.getStartDate(), reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getActivePatientPercentage (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getActivePatientPercentage(reportSearchParams.getStartDate(),reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getDispenseByAge (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getDispenseByAge(reportSearchParams.getStartDate(), reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getDispensesByGender (int year, String clinicId, String serviceCode) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getDispensesByGender(reportSearchParams.getStartDate(), reportSearchParams.getEndDate(), clinicId, serviceCode)
    }

    def getStockAlert (String clinicId, String serviceCode) {
        respond dashBoardService.getStockAlert(clinicId, serviceCode)
    }

    def getStockAlertAllServices (String clinicId) {
        List<ClinicalService> clinicalServices = ClinicalService.findAll()
        List<StockAlert> results = new ArrayList<>()
        clinicalServices.each {it ->
            dashBoardService.getStockAlert(clinicId, it.code).each {item ->
                results.add(item)
            }
        }
        respond results
    }

    def getDashboardServiceButton (int year, String clinicId) {
        ReportSearchParams reportSearchParams = ReportSearchParams.generateAnnualPeriod(year)

        respond dashBoardService.getDashboardServiceButton(reportSearchParams.getStartDate(),reportSearchParams.getEndDate(), clinicId)
    }
}
