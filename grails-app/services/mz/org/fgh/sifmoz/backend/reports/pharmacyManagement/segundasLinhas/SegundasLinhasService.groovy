package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.segundasLinhas

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(SegundasLinhasReport)
abstract class SegundasLinhasService implements ISegundasLinhasService {
    @Autowired
    IPackService packService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService
    @Autowired
    IPatientVisitDetailsService iPatientVisitDetailsService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    @Override
    List<SegundasLinhasReport> getSegundasLinhasReportById(String reportId) {
        return SegundasLinhasReport.findAllByReportId(reportId)
    }

    @Override
    void processReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {

        def clinicalServiceParams = ClinicalService.findById(searchParams.clinicalService)
        def clinicParams = Clinic.findById(searchParams.clinicId)

        def result = packService.getSegundasLinhas(clinicalServiceParams, clinicParams, searchParams.getStartDate(), searchParams.getEndDate())

        String reportId = searchParams.getId()

        if (result == null || result.size() == 0) {
            processMonitor.setProgress(100)
            processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
            reportProcessMonitorService.save(processMonitor)
        } else {
            double percentageUnit = 100.0 / result.size()
            if (Utilities.listHasElements(result)) {
                for (linha in result) {
                    linha.reportId = searchParams.id
                    linha.periodType = searchParams.periodType
                    linha.period = searchParams.period
                    linha.year = searchParams.year
                    linha.startDate = searchParams.startDate
                    linha.endDate = searchParams.endDate
                    linha.clinicId = searchParams.clinicId
                }
                for (segundaLinhaReport in result) {
                    processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                    if (processMonitor.getProgress() > 100) processMonitor.setProgress(100)
                    if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
                        setProcessMonitor(processMonitor)
                    }
                    ReportProcessMonitor.withNewTransaction {
                        processMonitor.save(flush: true)
                    }
                    save(segundaLinhaReport)
                }
                processMonitor.setProgress(100)
                processMonitor.setMsg("Processamento terminado")
                try {
                    reportProcessMonitorService.save(processMonitor)
                } catch (Exception e) {
                    e.printStackTrace()
                    processMonitor.setProgress(100)
                    processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
                    reportProcessMonitorService.save(processMonitor)
                }
            }
        }
    }

    private static Date determineDate(Date date, int value) {
        return ConvertDateUtils.addMonth(date, value)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }

    def serviceMethod() {

    }
}
