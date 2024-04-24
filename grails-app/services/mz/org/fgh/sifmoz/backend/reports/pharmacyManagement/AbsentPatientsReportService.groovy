package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.sql.GroovyRowResult
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.patients.ActivePatientReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

import java.text.DecimalFormat

@Transactional
@Service(AbsentPatientsReport)
abstract class AbsentPatientsReportService implements IAbsentPatientsReportService{
    @Autowired
    IPackService packService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    @Override
    List<AbsentPatientsReport> getReportDataByReportId(String reportId) {
        return AbsentPatientsReport.findAllByReportId(reportId)
    }

    @Override
    void processReportAbsentDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        Clinic clinic = Clinic.findById(searchParams.clinicId)
        ClinicalService clinicalService = ClinicalService.findById(searchParams.clinicalService)
        List absentReferredPatients
        if (searchParams.reportType.equalsIgnoreCase("FALTOSOS_AO_LEVANTAMENTO_APSS")) {
            absentReferredPatients = packService.getAbsentPatientsApssByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,searchParams.startDate,searchParams.endDate)
        } else if (searchParams.reportType.equalsIgnoreCase("FALTOSOS_AO_LEVANTAMENTO")) {
            absentReferredPatients = packService.getAbsentPatientsByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,searchParams.startDate,searchParams.endDate)
        } else if (searchParams.reportType.equalsIgnoreCase("FALTOSOS_AO_LEVANTAMENTO_DT")) {
            absentReferredPatients = packService.getAbsentPatientsDTByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,searchParams.startDate,searchParams.endDate)
        } else if (searchParams.reportType.equalsIgnoreCase("FALTOSOS_AO_LEVANTAMENTO_DS")) {
            absentReferredPatients = packService.getAbsentPatientsDSByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,searchParams.startDate,searchParams.endDate)
        }

        String reportId = searchParams.getId()
        List<AbsentPatientsReport> resultList = new ArrayList<>()


        if (absentReferredPatients.size() == 0) {
            processMonitor.setProgress(100)
            processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
            reportProcessMonitorService.save(processMonitor)
        } else {
            double percentageUnit = 100.0 / absentReferredPatients.size()

            if (Utilities.listHasElements(absentReferredPatients)) {


            for (int i = 0; i < absentReferredPatients.size(); i++) {
                AbsentPatientsReport absentPatient = new AbsentPatientsReport()
                absentPatient.setClinic(clinic.clinicName)
                absentPatient.setStartDate(searchParams.startDate)
                absentPatient.setEndDate(searchParams.endDate)
                absentPatient.setClinicalServiceId(clinicalService.code)
                absentPatient.setReportId(searchParams.id)
                absentPatient.setPeriodType(searchParams.periodType)
                absentPatient.setReportId(searchParams.id)
                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)

                generateAndSaveAbsentPatient(absentReferredPatients[i], absentPatient, reportId, searchParams)

                resultList.add(absentPatient)
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

    void generateAndSaveAbsentPatient (def item, AbsentPatientsReport absentPatient, String reportId, ReportSearchParams searchParams) {
        if (searchParams.reportType.equalsIgnoreCase("FALTOSOS_AO_LEVANTAMENTO_APSS")) {
            absentPatient.setNid(item["value"].toString())
            absentPatient.setReportId(reportId)
            def firstName = String.valueOf(item["first_names"]) == null || (String.valueOf(item["first_names"])).equalsIgnoreCase("null") ? "" : String.valueOf(item["first_names"])
            def lastName = String.valueOf(item["last_names"]) == null || (String.valueOf(item["last_names"])).equalsIgnoreCase("null") ? "" : String.valueOf(item["last_names"])
            absentPatient.setName(firstName + ' ' + lastName)
            if (item["contact"] != null) {
                absentPatient.setContact(String.valueOf(item["contact"]))
            }
            if (item["address"] != null) {
                absentPatient.setAddress(String.valueOf(item["address"]))
            }
            if (item["idade"] != null) {
                absentPatient.setIdade(String.valueOf(item["idade"]).contains(".") ? (int)Double.parseDouble(String.valueOf(item["idade"])):Integer.parseInt((String.valueOf(item["idade"]))))
            }
            if (item["served_service"] != null) {
                absentPatient.setServedService(String.valueOf(item["served_service"]))
            }

            AbsentPatientsReport.withNewTransaction {
                absentPatient.save(flush:true)
            }
        } else {
            absentPatient.setNid(item[0].toString())
            absentPatient.setReportId(reportId)
            def firstName = String.valueOf(item[1]) == null || (String.valueOf(item[1])).equalsIgnoreCase("null") ? "" : String.valueOf(item[1])
            def lastName = String.valueOf(item[3]) == null || (String.valueOf(item[3])).equalsIgnoreCase("null") ? "" : String.valueOf(item[3])
            absentPatient.setName(firstName + ' ' + lastName)

            absentPatient.setDateMissedPickUp(item[6] as Date)
            Date abandonmentDate = ConvertDateUtils.addDaysDate(absentPatient.dateMissedPickUp, 60)
            if (searchParams.endDate.after(abandonmentDate)) {
                absentPatient.setDateIdentifiedAbandonment(abandonmentDate)
            }
            if (item[4] != null) {
                absentPatient.setContact(String.valueOf(item[4]))
            }


        }
        save(absentPatient)

    }
}
