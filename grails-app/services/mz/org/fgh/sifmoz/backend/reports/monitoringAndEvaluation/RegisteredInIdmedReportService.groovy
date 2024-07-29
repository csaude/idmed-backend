package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(RegisteredInIdmedReportService)
abstract class RegisteredInIdmedReportService implements IRegisteredInIdmedReport {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        def result = Patient.findAllByHisIsNullAndCreationDateBetween(
                searchParams.getStartDate(),
                searchParams.getEndDate())

        double percentageUnit = 0
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / result.size()
        }
        for (Patient patientRegisteredInIdmed: result) {
            List<PatientServiceIdentifier> identifiers = PatientServiceIdentifier.findAllByPatientAndPrefered(patientRegisteredInIdmed, true)

            RegisteredInIdmedReport registeredInIdmedReport= new RegisteredInIdmedReport()
            registeredInIdmedReport.setStartDate(searchParams.startDate)
            registeredInIdmedReport.setEndDate(searchParams.endDate)
            registeredInIdmedReport.setReportId(searchParams.id)
            registeredInIdmedReport.setPeriodType(searchParams.periodType)
            registeredInIdmedReport.setYear(searchParams.year)
            registeredInIdmedReport.setPharmacyId(searchParams.clinicId)
            registeredInIdmedReport.setProvinceId(searchParams.provinceId)
            registeredInIdmedReport.setDistrictId(searchParams.districtId)

            registeredInIdmedReport.setNid(identifiers.isEmpty() ? 'Sem NID' : identifiers?.last()?.value)
            registeredInIdmedReport.setCreationDate(patientRegisteredInIdmed.creationDate)

            registeredInIdmedReport.setFirstName(patientRegisteredInIdmed.firstNames)
            registeredInIdmedReport.setLastName(patientRegisteredInIdmed.lastNames)
            registeredInIdmedReport.setDateOfBirth(patientRegisteredInIdmed.dateOfBirth)
            registeredInIdmedReport.setGender(patientRegisteredInIdmed.gender)
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            reportProcessMonitorService.save(processMonitor)
            save(registeredInIdmedReport)
        }
        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)

    }

    @Override
    List<RegisteredInIdmedReport> getReportDataByReportId(String reportId) {
        return RegisteredInIdmedReport.findAllByReportId(reportId)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }

}
