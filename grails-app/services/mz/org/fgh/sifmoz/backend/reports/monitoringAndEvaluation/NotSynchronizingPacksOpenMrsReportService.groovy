package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.openmrsErrorLog.OpenmrsErrorLog
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(NotSynchronizingPacksOpenMrsReport)
abstract class NotSynchronizingPacksOpenMrsReportService implements INotSynchronizingPacksOpenMrsReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"


    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        def result = OpenmrsErrorLog.findAllByDateCreatedBetween(
                searchParams.getStartDate(),
                searchParams.getEndDate())
        double percentageUnit = 0
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / result.size()
        }
        for (OpenmrsErrorLog openmrsErrorLog:result) {

            NotSynchronizingPacksOpenMrsReport notSynchroPack = new NotSynchronizingPacksOpenMrsReport()
            notSynchroPack.setStartDate(searchParams.startDate)
            notSynchroPack.setEndDate(searchParams.endDate)
            notSynchroPack.setReportId(searchParams.id)
            notSynchroPack.setPeriodType(searchParams.periodType)
            notSynchroPack.setYear(searchParams.year)
            notSynchroPack.setErrorDescription(openmrsErrorLog.errorDescription)
            notSynchroPack.setDateCreated(openmrsErrorLog.dateCreated)
            notSynchroPack.setNid(openmrsErrorLog.nid)
            notSynchroPack.setPatient(openmrsErrorLog.patient)
            notSynchroPack.setReturnPickupDate(openmrsErrorLog.returnPickupDate)
            notSynchroPack.setJsonRequest(openmrsErrorLog.jsonRequest)
            notSynchroPack.setPickupDate(openmrsErrorLog.pickupDate)
            notSynchroPack.setClinicalService(openmrsErrorLog.servicoClinico)
            notSynchroPack.setPatientVisitDetails(openmrsErrorLog.patientVisitDetails)
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            reportProcessMonitorService.save(processMonitor)
            save(notSynchroPack)
        }
        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)

    }

    @Override
    List<NotSynchronizingPacksOpenMrsReport> getReportDataByReportId(String reportId) {
        return NotSynchronizingPacksOpenMrsReport.findAllByReportId(reportId)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }


}
