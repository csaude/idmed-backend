package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement

import grails.converters.JSON
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.referralManagement.ReferredPatientsReport
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional


class AbsentPatientsReportController extends MultiThreadRestReportController{

    IAbsentPatientsReportService absentPatientsReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    AbsentPatientsReportController() {
        super(ReferredPatientsReport)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond absentPatientsReportService.list(params), model:[absentPatientsReportCount: absentPatientsReportService.count()]
    }

    def show(Long id) {
        respond absentPatientsReportService.get(id)
    }

    @Transactional
    def save(AbsentPatientsReport absentPatientsReport) {
        if (absentPatientsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (absentPatientsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond absentPatientsReport.errors
            return
        }

        try {
            absentPatientsReportService.save(absentPatientsReport)
        } catch (ValidationException e) {
            respond absentPatientsReport.errors
            return
        }

        respond absentPatientsReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(AbsentPatientsReport absentPatientsReport) {
        if (absentPatientsReport == null) {
            render status: NOT_FOUND
            return
        }
        if (absentPatientsReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond absentPatientsReport.errors
            return
        }

        try {
            absentPatientsReportService.save(absentPatientsReport)
        } catch (ValidationException e) {
            respond absentPatientsReport.errors
            return
        }

        respond absentPatientsReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || absentPatientsReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def initReportProcess (ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render getProcessStatus() as JSON
        doProcessReport()
    }


    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }

    @Override
    void run() {
        absentPatientsReportService.processReportAbsentDispenseRecords(searchParams, this.processStatus)
    }
    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    def deleteByReportId(String reportId) {
        List<AbsentPatientsReport> absentPatientsReports = AbsentPatientsReport.findAllByReportId(reportId)
        AbsentPatientsReport.deleteAll(absentPatientsReports)
        render status: NO_CONTENT
    }

    def printReport(String reportId,String fileType) {

//        String reportPathFile = getReportsPath()+"pharmacyManagement/RelatorioAbandonosFaltososPersonalizado.jrxml"
//        List<AbsentPatientsReport> absentPatientsReports = AbsentPatientsReport.findAllByReportId(reportId)
        List<AbsentPatientsReport> absentPatientsReports = absentPatientsReportService.getReportDataByReportId(reportId)
//        Map<String, Object> map = new HashMap<>()
//        if (absentPatientsReports.size() > 0) {
//            // map.put("path", getReportsPath())
//            map.put("mainfacilityname", absentPatientsReports.get(0).getPharmacyId().isEmpty() ? "" : Clinic.findById(absentPatientsReports.get(0).getPharmacyId()).getClinicName())
//            map.put("dateEnd", absentPatientsReports.get(0).getEndDate())
//            map.put("date", absentPatientsReports.get(0).getStartDate())
//
//            byte[] report = super.printReport(reportId, fileType, reportPathFile, map, absentPatientsReports)
//            render(file: report, contentType: 'application/'+fileType.equals("PDF")? 'pdf' : 'xls')
//        } else {
//            render status: NO_CONTENT
//        }
//        println(absentPatientsReports.size())
        render absentPatientsReports as JSON
    }
}
