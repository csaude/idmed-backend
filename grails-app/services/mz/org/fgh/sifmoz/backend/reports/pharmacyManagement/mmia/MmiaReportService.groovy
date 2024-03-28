package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.gorm.services.Service
import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired


@Transactional
@Service(MmiaReport)
abstract class MmiaReportService implements IMmiaReportService {

    @Autowired
    IPackService packService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService
    @Autowired
    IPatientVisitDetailsService iPatientVisitDetailsService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    @Override
    void processReport(ReportSearchParams searchParams, MmiaReport curMmiaReport, ReportProcessMonitor processMonitor) {
        if(searchParams.reportType.equalsIgnoreCase("MAPA_MENSAL_DE_MEDICAMENTO_DA_TB")){
            try {
//                def clinicalServiceParams = ClinicalService.findById('80A7852B-57DF-4E40-90EC-ABDE8403E01F') // Forcando TARVs
                def clinicalServiceParams = ClinicalService.findById(searchParams.getClinicalService())
                def clinicParams = Clinic.findById(searchParams.getClinicId())

                double percentageUnit = 65 / 3
                // 3 Steps
                ReportProcessMonitor.withTransaction {
                    processMonitor.setProgress(processMonitor.getProgress() + 5)
                    processMonitor.save(flush:true)
                }

                //Step 1

                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)

                // Step 2 Add DT and DS Later Statistics
                percentageUnit = 65 / 2


                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)
                curMmiaReport.save(flush: true)

                // Step 3 Add Regimen Statistics [VOU REUSAR 'MmiaRegimenSubReport' PARA 'Faixas Etárias' de forma provisoria]
                percentageUnit = 65 - processMonitor.getProgress()
                List<MmiaRegimenSubReport> regimenSubReportList = new ArrayList<>()

                regimenSubReportList = packService.getMMIARegimenStatisticTB(clinicalServiceParams, clinicParams, searchParams.getStartDate(), searchParams.getEndDate())

                saveMmiaRegimenItems(regimenSubReportList, curMmiaReport)

                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)
            } catch (Exception e) {
                e.printStackTrace()
                processMonitor.setProgress(100)
                processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
                reportProcessMonitorService.save(processMonitor)
            }
        } else{
            try {
                def clinicalServiceParams = ClinicalService.findById(searchParams.getClinicalService())
                def clinicParams = Clinic.findById(searchParams.getClinicId())

                double percentageUnit = 65 / 3
                // 3 Steps
                ReportProcessMonitor.withTransaction {
                    processMonitor.setProgress(processMonitor.getProgress() + 5)
                    processMonitor.save(flush:true)
                }

                //Step 1 Add Current Month Statistics
                curMmiaReport = packService.getMMIAPatientStatisticOnPeriod(curMmiaReport, clinicalServiceParams, clinicParams, searchParams.getStartDate(), searchParams.getEndDate())

                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)

                // Step 2 Add DT and DS Later Statistics
                percentageUnit = 65 / 2

                def dispenseTypeStatisticM1 = packService.getMMIADispenseTypeStatisticOnPeriod(clinicalServiceParams, clinicParams, determineDate(searchParams.getStartDate(), -1), determineDate(searchParams.getEndDate(), -1))
                def dispenseTypeStatisticM2 = packService.getMMIADispenseTypeStatisticOnPeriod(clinicalServiceParams, clinicParams, determineDate(searchParams.getStartDate(), -2), determineDate(searchParams.getEndDate(), -2))
                def dispenseTypeStatisticM3 = packService.getMMIADispenseTypeStatisticOnPeriod(clinicalServiceParams, clinicParams, determineDate(searchParams.getStartDate(), -3), determineDate(searchParams.getEndDate(), -3))
                def dispenseTypeStatisticM4 = packService.getMMIADispenseTypeStatisticOnPeriod(clinicalServiceParams, clinicParams, determineDate(searchParams.getStartDate(), -4), determineDate(searchParams.getEndDate(), -4))
                def dispenseTypeStatisticM5 = packService.getMMIADispenseTypeStatisticOnPeriod(clinicalServiceParams, clinicParams, determineDate(searchParams.getStartDate(), -5), determineDate(searchParams.getEndDate(), -5))

                if (dispenseTypeStatisticM1 != null) {
                    curMmiaReport.dtM1 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM1[1]))
                    curMmiaReport.dsM1 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM1[2]))
                    curMmiaReport.dbM1 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM1[3]))
                }

                if (dispenseTypeStatisticM2 != null) {
                    curMmiaReport.dtM2 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM2[1]))
                    curMmiaReport.dsM2 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM2[2]))
                }

                if (dispenseTypeStatisticM3 != null) {
                    curMmiaReport.dsM3 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM3[2]))
                }

                if (dispenseTypeStatisticM4 != null) {
                    curMmiaReport.dsM4 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM4[2]))
                }

                if (dispenseTypeStatisticM5 != null) {
                    curMmiaReport.dsM5 = Integer.valueOf(String.valueOf(dispenseTypeStatisticM5[2]))
                }

                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)
                curMmiaReport.save(flush: true)

                // Step 3 Add Regimen Statistics
                percentageUnit = 65 - processMonitor.getProgress()
                List<MmiaRegimenSubReport> regimenSubReportList = new ArrayList<>()

                regimenSubReportList = packService.getMMIARegimenStatistic(clinicalServiceParams, clinicParams, searchParams.getStartDate(), searchParams.getEndDate())

                saveMmiaRegimenItems(regimenSubReportList, curMmiaReport)

                processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
                processMonitor.save(flush: true)
            } catch (Exception e) {
                e.printStackTrace()
                processMonitor.setProgress(100)
                processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
                reportProcessMonitorService.save(processMonitor)
            }
        }

    }

    private static Date determineDate(Date date, int value) {
        return ConvertDateUtils.addMonth(date, value)
    }

    private static void saveMmiaRegimenItems(List<MmiaRegimenSubReport> regimenSubReportList, MmiaReport curMmiaReport) {
        for (MmiaRegimenSubReport mmiaRegimenSubReport : regimenSubReportList) {
            mmiaRegimenSubReport.reportId = curMmiaReport.reportId
            mmiaRegimenSubReport.mmiaReport = curMmiaReport
            mmiaRegimenSubReport.save(flush: true)
        }
    }
}