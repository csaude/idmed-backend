package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(ArvDailyRegisterReportTemp)
abstract class ArvDailyRegisterReportService implements IArvDailyRegisterReportService {

    PatientVisitDetailsService patientVisitDetailsService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService


    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"



    private static ArvDailyRegisterReportTemp setGenericInfo(ReportSearchParams searchParams) {
        ArvDailyRegisterReportTemp arvTemp = new ArvDailyRegisterReportTemp()
        arvTemp.setStartDate(searchParams.startDate)
        arvTemp.setEndDate(searchParams.endDate)
        arvTemp.setReportId(searchParams.id)
        arvTemp.setPeriodType(searchParams.periodType)
        arvTemp.setReportId(searchParams.id)
        arvTemp.setYear(searchParams.year)
        return arvTemp
    }


    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        def result

        // NOVOS REPORTS [Para nao criar controllers novos]
        if (searchParams.reportType.equalsIgnoreCase('LIVRO_DIARIO_TPT')) {
            result = patientVisitDetailsService.getTPTDailyReport(searchParams.getClinicId(),
                    searchParams.getStartDate(),
                    searchParams.getEndDate(), searchParams.getClinicalService())
        } else if(searchParams.reportType.equalsIgnoreCase('LIVRO_DIARIO_PREP')) {
            result = patientVisitDetailsService.getPREPDailyReport(searchParams.getClinicId(),
                searchParams.getStartDate(),
                searchParams.getEndDate(), searchParams.getClinicalService())
        } else {
            result = patientVisitDetailsService.getARVDailyReport(searchParams.getClinicId(),
                    searchParams.getStartDate(),
                    searchParams.getEndDate(), searchParams.getClinicalService())
        }

        double percentageUnit = 0
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / result.size()
        }
        int orderNumber = 0

        for (item in result) {
            List arvTemp = item as List
            ArvDailyRegisterReportTemp arvDailyRegisterReportTemp

            orderNumber++

            // Crie um novo ArvDailyRegisterReportTemp
            arvDailyRegisterReportTemp = setGenericInfo(searchParams)
            arvDailyRegisterReportTemp.setOrderNumber(orderNumber.toString())
            arvDailyRegisterReportTemp.setNid arvTemp[0] == null ? "":(arvTemp[0].toString())
            arvDailyRegisterReportTemp.setPatientName(arvTemp[1] +" "+arvTemp[2] +" " +arvTemp[3] )
            Integer age =  arvTemp[5] == null? 0 :  ConvertDateUtils.getAgeByLocalDates(arvTemp[5] as Date, searchParams.getEndDate())
            arvDailyRegisterReportTemp.setAgeGroup_0_4(age >= 0 && age < 4 ? "Sim" : "Nao")
            arvDailyRegisterReportTemp.setAgeGroup_5_9(age >= 5 && age <= 9 ? "Sim" : "Nao")
            arvDailyRegisterReportTemp.setAgeGroup_10_14(age >= 10 && age <= 14 ? "Sim" : "Nao")
            arvDailyRegisterReportTemp.setAgeGroup_Greater_than_15(age >= 15 ? "Sim" : "Nao")
            arvDailyRegisterReportTemp.setPatientType( arvTemp[4] == null ? "": arvTemp[4].toString())
            arvDailyRegisterReportTemp.setDispensationType(arvTemp[6] == null? "":arvTemp[6].toString())
            arvDailyRegisterReportTemp.setTherapeuticLine(arvTemp[7]==null ? "": arvTemp[7].toString())
            arvDailyRegisterReportTemp.setPickupDate(arvTemp[8]==null ? "": arvTemp[8] as Date)
            arvDailyRegisterReportTemp.setNextPickupDate(arvTemp[9]==null ? "": arvTemp[9] as Date)
            arvDailyRegisterReportTemp.setRegime(arvTemp[10]==null ? "": arvTemp[10].toString())
            arvDailyRegisterReportTemp.setId(arvTemp[11]==null ? "": arvTemp[11].toString())
            arvDailyRegisterReportTemp.setStartReason(arvTemp[12]==null ? "": arvTemp[12].toString())
            arvDailyRegisterReportTemp.setPrep((arvTemp[13]).toString().equalsIgnoreCase("PREP")  ? "SIM" : "")
            arvDailyRegisterReportTemp.setPatientVisitDetailId(arvTemp[15]==null ? "": arvTemp[15].toString())

            // Salve o novo ArvDailyRegisterReportTemp
            save(arvDailyRegisterReportTemp)

            // Crie e associe um DrugQuantityTemp
            DrugQuantityTemp prod = new DrugQuantityTemp()
            prod.setDrugName(String.valueOf(arvTemp[16]))
            prod.setQuantity((Long) Double.parseDouble(arvTemp[17].toString()))
            prod.setNid(arvTemp[0].toString())
            prod.setArvDailyRegisterReportTemp(arvDailyRegisterReportTemp)
            save(prod)
            arvDailyRegisterReportTemp.addToDrugQuantityTemps(prod)

            // Atualize o progresso e salve o processo de monitoramento, se necessário
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            reportProcessMonitorService.save(processMonitor)
        }


        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)
    }

    @Override
    List<ArvDailyRegisterReportTemp> getReportDataByReportId(String reportId) {
        return ArvDailyRegisterReportTemp.findAllByReportId(reportId)
    }

    @Override
    List<DrugQuantityTemp> getSubReportDataById(String id) {
        ArvDailyRegisterReportTemp arvDailyRegisterReportTemp = ArvDailyRegisterReportTemp.findById(id)
            List<DrugQuantityTemp> list = DrugQuantityTemp.executeQuery("select  s from DrugQuantityTemp  s "  +
                    " where s.arvDailyRegisterReportTemp =:arvDailyRegisterReportTemp ",
                    [arvDailyRegisterReportTemp: arvDailyRegisterReportTemp]);
            return list
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }

}
