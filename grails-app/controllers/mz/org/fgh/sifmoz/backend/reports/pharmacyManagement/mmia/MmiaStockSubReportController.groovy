package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.converters.JSON
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

class MmiaStockSubReportController extends MultiThreadRestReportController<MmiaStockSubReportItem>{

    IMmiaStockSubReportService mmiaStockSubReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    MmiaStockSubReportController() {
        super(MmiaStockSubReportItem)
    }

    @Override
    protected String getProcessingStatusMsg() {
        return null
    }

    def printReport(String reportId, String fileType) {

    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond mmiaStockSubReportService.list(params), model:[mmiaStockSubReportCount: mmiaStockSubReportService.count()]
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(MmiaStockSubReportItem.findByReportId(id)) as JSON
    }

    @Transactional
    def save(MmiaStockSubReportItem mmiaStockSubReport) {
        if (mmiaStockSubReport == null) {
            render status: NOT_FOUND
            return
        }
        if (mmiaStockSubReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond mmiaStockSubReport.errors
            return
        }

        try {
            mmiaStockSubReportService.save(mmiaStockSubReport)
        } catch (ValidationException e) {
            respond mmiaStockSubReport.errors
            return
        }

        respond mmiaStockSubReport, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(MmiaStockSubReportItem mmiaStockSubReport) {
        if (mmiaStockSubReport == null) {
            render status: NOT_FOUND
            return
        }
        if (mmiaStockSubReport.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond mmiaStockSubReport.errors
            return
        }

        try {
            mmiaStockSubReportService.save(mmiaStockSubReport)
        } catch (ValidationException e) {
            respond mmiaStockSubReport.errors
            return
        }

        respond mmiaStockSubReport, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || mmiaStockSubReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    @Override
    void run() {

    }
}
