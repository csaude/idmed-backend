package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.patientUpdateOpenMrsErrorLog.PatientUpdateOpenMrsErrorLog
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(ErrorLogPatientUpdateOpenMrsReport)
abstract class ErrorLogPatientUpdateOpenMrsReportService implements IErrorLogPatientUpdateOpenMrsReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"


    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        def result = PatientUpdateOpenMrsErrorLog.findAllByDateCreatedBetween(
                searchParams.getStartDate(),
                searchParams.getEndDate())
        double percentageUnit = 0
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / result.size()
        }
        for (PatientUpdateOpenMrsErrorLog openmrsErrorLog:result) {

            ErrorLogPatientUpdateOpenMrsReport patientUpdateErrorLog = new ErrorLogPatientUpdateOpenMrsReport()
            patientUpdateErrorLog.setStartDate(searchParams.startDate)
            patientUpdateErrorLog.setEndDate(searchParams.endDate)
            patientUpdateErrorLog.setReportId(searchParams.id)
            patientUpdateErrorLog.setPeriodType(searchParams.periodType)
            patientUpdateErrorLog.setYear(searchParams.year)
            patientUpdateErrorLog.setErrorDescription(openmrsErrorLog.errorDescription)
            patientUpdateErrorLog.setDateCreated(openmrsErrorLog.dateCreated)
            patientUpdateErrorLog.setNid(openmrsErrorLog.nid)
            patientUpdateErrorLog.setPatient(openmrsErrorLog.patient)
            patientUpdateErrorLog.setClinicalService(openmrsErrorLog.servicoClinico)
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            reportProcessMonitorService.save(processMonitor)
            save(patientUpdateErrorLog)
        }
        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)

    }

    @Override
    List<ErrorLogPatientUpdateOpenMrsReport> getReportDataByReportId(String reportId) {
        return ErrorLogPatientUpdateOpenMrsReport.findAllByReportId(reportId)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }


}
