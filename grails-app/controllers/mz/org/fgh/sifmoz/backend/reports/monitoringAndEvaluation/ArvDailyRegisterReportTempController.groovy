package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

import static org.springframework.http.HttpStatus.*

class ArvDailyRegisterReportTempController extends MultiThreadRestReportController {

    IArvDailyRegisterReportService arvDailyRegisterReportService
    @Autowired
    protected SessionFactory sessionFactory;

    // def sessionFactory = SessionFactory.newInstance()

    DataSource dataSource
    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ArvDailyRegisterReportTempController() {
        super(ArvDailyRegisterReportTemp)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond arvDailyRegisterReportService.list(params), model: [referredPatientsReportCount: arvDailyRegisterReportService.count()]
    }

    def show(Long id) {
        respond arvDailyRegisterReportService.get(id)
    }

    @Transactional
    def save(ArvDailyRegisterReportTemp reportTemp) {
        if (reportTemp == null) {
            render status: NOT_FOUND
            return
        }
        if (reportTemp.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond reportTemp.errors
            return
        }

        try {
            arvDailyRegisterReportService.save(reportTemp)
        } catch (ValidationException e) {
            respond reportTemp.errors
            return
        }

        respond reportTemp, [status: CREATED, view: "show"]
    }


    @Transactional
    def update(ArvDailyRegisterReportTemp reportTemp) {
        if (reportTemp == null) {
            render status: NOT_FOUND
            return
        }
        if (reportTemp.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond reportTemp.errors
            return
        }

        try {
            IArvDailyRegisterReportService.save(reportTemp)
        } catch (ValidationException e) {
            respond reportTemp.errors
            return
        }

        respond reportTemp, [status: OK, view: "show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || arvDailyRegisterReportService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def initReportProcess(ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render JSONSerializer.setJsonObjectResponse(this.processStatus) as JSON
        doProcessReport()
    }
    /**
     * process report
     */
    long getRecordsQtyToProcess() {
        return 0
    }

    void getProcessedRecordsQty(String reportId) {

    }

    def printReport(String reportId, String fileType) {
        List<ArvDailyRegisterReportTemp> itemsReport = arvDailyRegisterReportService.getReportDataByReportId(reportId)
        for (ArvDailyRegisterReportTemp item : itemsReport) {
            item.setDrugQuantityTemps(arvDailyRegisterReportService.getSubReportDataById(item.getId()))
        }
        if (Utilities.listHasElements(itemsReport)) { 
          render  JSONSerializer.setObjectListJsonResponse(itemsReport) as JSON
        } else {
            render status: NO_CONTENT
        }
    }

    @Override
    void run() {
        arvDailyRegisterReportService.processReportRecords(searchParams, this.processStatus)

    }

    protected int countProcessedRecs() {
        return 0
    }

    protected int countRecordsToProcess() {
        return 0
    }

    def getProcessingStatus(String reportId) {
        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
    }

    @Override
    protected String getProcessingStatusMsg() {
        if (!Utilities.stringHasValue(processStage)) processStage = PROCESS_STATUS_INITIATING
        return processStage
    }
}
