package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(PossiblePatientDuplicatesReport)
abstract class PossiblePatientDuplicatesService implements IPossiblePatientDuplicatesService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IPatientService patientService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"


    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        def result = patientService.findPossibleDuplicatePatients()
        double percentageUnit = 0
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / result.size()
        }
        for (Object possiblePatientDuplicatesRepo:result) {

            PossiblePatientDuplicatesReport possiblePatientDuplicatesReport = new PossiblePatientDuplicatesReport()
         //   possiblePatientDuplicatesReport.setStartDate(searchParams.startDate)
          //  possiblePatientDuplicatesReport.setEndDate(searchParams.endDate)
            possiblePatientDuplicatesReport.setReportId(searchParams.id)
            possiblePatientDuplicatesReport.setPeriodType(searchParams.periodType)
            possiblePatientDuplicatesReport.setYear(searchParams.year)
            possiblePatientDuplicatesReport.setNid(possiblePatientDuplicatesRepo[0] == null ? "": possiblePatientDuplicatesRepo[0].toString())
            possiblePatientDuplicatesReport.setPatientName(possiblePatientDuplicatesRepo[1] == null ? "": possiblePatientDuplicatesRepo[1].toString() +" " + possiblePatientDuplicatesRepo[2].toString())
            possiblePatientDuplicatesReport.setDateOfBirth(possiblePatientDuplicatesRepo[3] == null ? "": possiblePatientDuplicatesRepo[3] as Date)
            possiblePatientDuplicatesReport.setGender(possiblePatientDuplicatesRepo[4] == null ? "": possiblePatientDuplicatesRepo[4].toString())
            possiblePatientDuplicatesReport.setNumberOfTimes(possiblePatientDuplicatesRepo[5] == null ? "": possiblePatientDuplicatesRepo[5].toString())
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            reportProcessMonitorService.save(processMonitor)
            save(possiblePatientDuplicatesReport)
        }
        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)

    }

    @Override
    List<PossiblePatientDuplicatesReport> getReportDataByReportId(String reportId) {
        return PossiblePatientDuplicatesReport.findAllByReportId(reportId)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }


}
