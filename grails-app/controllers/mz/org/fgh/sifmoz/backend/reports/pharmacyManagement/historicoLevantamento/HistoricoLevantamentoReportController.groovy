package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.historicoLevantamento

import grails.converters.JSON
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class HistoricoLevantamentoReportController extends MultiThreadRestReportController {
    IHistoricoLevantamentoReportService historicoLevantamentoReportService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    HistoricoLevantamentoReportController() {
        super(HistoricoLevantamentoReport)
    }

    def index() { }

    /**
     * Implementa toda a logica de processamento da informação do relatório
     */
    @Override
    void run() {
        processHistryReport()
    }

    def initReportProcess (ReportSearchParams searchParams) {
        super.initReportParams(searchParams)
        render getProcessStatus() as JSON
        doProcessReport()
    }

    void processHistryReport() {
        historicoLevantamentoReportService.processamentoDados(getSearchParams(),  this.processStatus)
    }

    def printReport(String reportId, String fileType) {
        List<HistoricoLevantamentoReport> reportObjects = historicoLevantamentoReportService.getReportDataByReportId(reportId)
        render reportObjects as JSON
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
