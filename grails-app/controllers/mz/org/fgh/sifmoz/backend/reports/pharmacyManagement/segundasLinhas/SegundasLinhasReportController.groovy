package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.segundasLinhas

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.multithread.MultiThreadRestReportController
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.linhasUsadas.LinhasUsadasReport
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import mz.org.fgh.sifmoz.backend.utilities.Utilities

import static org.springframework.http.HttpStatus.*

                class SegundasLinhasReportController extends MultiThreadRestReportController{


                    ISegundasLinhasService segundasLinhasService
                //    LinhasUsadasReport curLinhaUsadaReport

                    static responseFormats = ['json', 'xml']
                    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

                //    protected IReportProcessMonitorService reportProcessMonitorService;

                    SegundasLinhasReportController() {
                        super(SegundasLinhasReport)
                    }

                    def index(Integer max) {
                        params.max = Math.min(max ?: 10, 100)
                        respond segundasLinhasService.list(params), model:[linhasUsadasReportCount: segundasLinhasService.count()]
                    }

                    def show(String id) {
                        respond segundasLinhasService.get(id)
                    }

                    @Transactional
                    def save(SegundasLinhasReport segundasLinhasReport) {
                        if (segundasLinhasReport == null) {
                            render status: NOT_FOUND
                            return
                        }
                        if (segundasLinhasReport.hasErrors()) {
                            transactionStatus.setRollbackOnly()
                            respond segundasLinhasReport.errors
                            return
                        }

                        try {
                            segundasLinhasService.save(segundasLinhasReport)
                        } catch (ValidationException e) {
                            respond segundasLinhasReport.errors
                            return
                        }

                        respond segundasLinhasReport, [status: CREATED, view:"show"]
                    }

                    @Transactional
                    def update(SegundasLinhasReport segundasLinhasReport) {
                        if (segundasLinhasReport == null) {
                            render status: NOT_FOUND
                            return
                        }
                        if (segundasLinhasReport.hasErrors()) {
                            transactionStatus.setRollbackOnly()
                            respond segundasLinhasReport.errors
                            return
                        }

                        try {
                            segundasLinhasService.save(segundasLinhasReport)
                        } catch (ValidationException e) {
                            respond segundasLinhasReport.errors
                            return
                        }

                        respond segundasLinhasReport, [status: OK, view:"show"]
                    }

                    @Transactional
                    def delete(String id) {
                        if (id == null || segundasLinhasService.delete(id) == null) {
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
                        segundasLinhasService.processReport(searchParams, this.processStatus)
                    }

                    def printReport(String reportId, String fileType) {

                        List<SegundasLinhasReport> segundasLinhasReports = segundasLinhasService.getSegundasLinhasReportById(reportId)
                        render segundasLinhasReports as JSON
                    }

                    def getProcessingStatus(String reportId) {
                        render JSONSerializer.setJsonObjectResponse(reportProcessMonitorService.getByReportId(reportId)) as JSON
                    }

                }