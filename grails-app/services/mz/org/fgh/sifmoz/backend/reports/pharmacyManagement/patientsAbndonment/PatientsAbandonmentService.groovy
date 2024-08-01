package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbndonment

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.patientsAbandonment.PatientsAbandonmentReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(PatientsAbandonmentReport)
abstract class PatientsAbandonmentService implements IPatientsAbandonmentService{
    @Autowired
    IPackService packService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    @Override
    List<PatientsAbandonmentReport> getReportDataByReportId(String reportId) {
        return PatientsAbandonmentReport.findAllByReportId(reportId)
    }

    @Override
    void processReportAbandonmetDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        Clinic clinic = Clinic.findById(searchParams.clinicId)
        ClinicalService clinicalService = ClinicalService.findById(searchParams.clinicalService)
        List patientsAbandonmentList
        if(searchParams.reportType.equalsIgnoreCase("PATIENTES_ABANDONMENT")) {
            patientsAbandonmentList = packService.getAbandonmentByClinicalServiceAndClinicOnPeriod(clinicalService, clinic, searchParams.startDate, searchParams.endDate)
        }
        if(searchParams.reportType.equalsIgnoreCase("PATIENTES_ABANDONMENT_RETURNED")) {
            patientsAbandonmentList = packService.getAbandonmentAndReturnByClinicalServiceAndClinicOnPeriod(clinicalService, clinic, searchParams.startDate, searchParams.endDate)
        }

                String reportId = searchParams.getId()
        List<PatientsAbandonmentReport> resultList = new ArrayList<>()


        if (patientsAbandonmentList.size() == 0) {
            processMonitor.setProgress(100)
            processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
            reportProcessMonitorService.save(processMonitor)
        } else {
            double percentageUnit = 100.0 / patientsAbandonmentList.size()

            if (Utilities.listHasElements(patientsAbandonmentList)) {

                for (int i = 0; i < patientsAbandonmentList.size(); i++) {
                    PatientsAbandonmentReport abandonmentReport = new PatientsAbandonmentReport()
                    abandonmentReport.setClinic(clinic.clinicName)
                    abandonmentReport.setStartDate(searchParams.startDate)
                    abandonmentReport.setEndDate(searchParams.endDate)
                    abandonmentReport.setClinicalServiceId(clinicalService.code)
                    abandonmentReport.setReportId(searchParams.id)
                    abandonmentReport.setPeriodType(searchParams.periodType)
                    abandonmentReport.setReportId(searchParams.id)
                    abandonmentReport.setPeriod(searchParams.year.toString())
                    processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)

                    generateAndSavePatientAbandonment(patientsAbandonmentList[i], abandonmentReport, reportId, searchParams)

                    resultList.add(abandonmentReport)
                    if (processMonitor.getProgress() > 99.6) {
                        processMonitor.setProgress(100)
                        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
                    }

                    ReportProcessMonitor.withNewTransaction {
                        processMonitor.save(flush: true)
                    }
                }
            }
        }
    }

    void generateAndSavePatientAbandonment(def item, PatientsAbandonmentReport patientsAbandonmentReport, String reportId, ReportSearchParams searchParams) {

        patientsAbandonmentReport.setNid(item[0].toString())
        patientsAbandonmentReport.setReportId(reportId)
        def firstName = String.valueOf(item[1]) == null || (String.valueOf(item[1])).equalsIgnoreCase("null") ? "" : String.valueOf(item[1])
        def lastName = String.valueOf(item[3]) == null || (String.valueOf(item[3])).equalsIgnoreCase("null") ? "" : String.valueOf(item[3])
        patientsAbandonmentReport.setName(firstName + ' ' + lastName)

        patientsAbandonmentReport.setDateMissedPickUp(item[5] as Date)
        patientsAbandonmentReport.setDateIdentifiedAbandonment(item[6] as Date)
        if(searchParams.reportType.equalsIgnoreCase("PATIENTES_ABANDONMENT_RETURNED")) {
            patientsAbandonmentReport.setReturnedPickUp(item[7] as Date)
        }
        if (item[4] != null) {
            patientsAbandonmentReport.setContact(String.valueOf(item[4]))
        }

        save(patientsAbandonmentReport)

    }
}
