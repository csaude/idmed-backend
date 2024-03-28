package mz.org.fgh.sifmoz.backend.multithread;

import grails.gorm.transactions.Transactional;
import grails.rest.RestfulController;
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils;
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor;
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService;
import mz.org.fgh.sifmoz.backend.report.ReportGenerator;
import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate5.SessionFactoryUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public abstract class MultiThreadRestReportController<T> extends RestfulController<T> implements ReportExecutor {
    protected ReportSearchParams searchParams;
    private static ExecutorService executor;
    protected String processStage;
    @Autowired
    protected SessionFactory sessionFactory;
    protected ReportProcessMonitor processStatus;
    public static final String PROCESS_STATUS_INITIATING = "Iniciando processamento";
    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado";
    @Autowired
    protected IReportProcessMonitorService reportProcessMonitorService;

    public MultiThreadRestReportController(Class<T> resource) {
        super(resource);
        executor = ExecutorThreadProvider.getInstance().getExecutorService();
    }

    public ReportSearchParams getSearchParams() {
        return searchParams;
    }

    @Transactional
    protected void initReportParams(ReportSearchParams searchParams) {
        this.searchParams = searchParams;
        this.searchParams.determineStartEndDate();
        this.processStatus = new ReportProcessMonitor(getSearchParams().getId(), getProcessingStatusMsg(), 5);
        reportProcessMonitorService.save(this.processStatus);

    }


    /**
     * Processa o relatório
     */
    protected void doProcessReport() {
        executor.execute(this);
        //  this.processStage = PROCESS_STATUS_PROCESSING_FINISHED;
        //  this.processStatus.setProgress(100);
        //  reportProcessMonitorService.save(this.processStatus);
    }

    protected void updateProcessingStatus() {
        this.processStatus.setProgress(100);
        reportProcessMonitorService.save(this.processStatus);
    }

  /*  protected void deleteByReportId(String reportId) {
        List<ReferredPatientsReport> referredPatientsReports = ReferredPatientsReport.findAllByReportId(reportId)
        ReferredPatientsReport.deleteAll(referredPatientsReports)
        render status: NO_CONTENT
    }*/

    public ReportProcessMonitor getProcessStatus() {
        return processStatus;
    }

    protected abstract String getProcessingStatusMsg();

    protected byte[] printReport(String reportId, String fileType, String report, Map<String, Object> params) throws SQLException {
        return this.printReport(reportId, fileType, report, params, null);
    }

    protected byte[] printReport(String reportId, String fileType, String report, Map<String, Object> params, Collection reportObjects) throws SQLException {
        params.put("path", getReportsPath());
        params.put("reportId", reportId);
        params.put("username", "Test_user");
        params.put("dataelaboracao", ConvertDateUtils.getCurrentDate());

        if ( CollectionUtils.isNotEmpty(reportObjects)) {
            return ReportGenerator.generateReport(params, fileType, reportObjects, report);
        } else {
            return ReportGenerator.generateReport(report, params, fileType, SessionFactoryUtils.getDataSource(sessionFactory).getConnection());
        }
    }

    protected String getReportsPath (){
        try {
            return grails.util.BuildSettings.BASE_DIR.getCanonicalPath()+"/"+"src/main/webapp/reports/";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
