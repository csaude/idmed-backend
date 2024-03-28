package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.historicoLevantamento

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.patients.ActivePatientReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.service.ClinicalServiceService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Transactional
@Service(HistoricoLevantamentoReport)
abstract class HistoricoLevantamentoReportService implements IHistoricoLevantamentoReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IPackService packService

    @Autowired
    SessionFactory sessionFactory

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Override
    HistoricoLevantamentoReport get(Serializable id) {
        return HistoricoLevantamentoReport.findById(id as String)
    }

    @Override
    List<HistoricoLevantamentoReport> list(Map args) {
        return null
    }

    @Override
    Long count() {
        return null
    }

    @Override
    HistoricoLevantamentoReport delete(Serializable id) {
        return null
    }

    @Override
    List<HistoricoLevantamentoReport> getReportDataByReportId(String reportId) {
        return HistoricoLevantamentoReport.findAllByReportId(reportId)
    }

    @Override
    List<HistoricoLevantamentoReport> processamentoDados(ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor) {
        Clinic clinic = Clinic.findById(reportSearchParams.clinicId)
        ClinicalService clinicalService = ClinicalService.findById(reportSearchParams.clinicalService)

        def result
        if(reportSearchParams.reportType.equalsIgnoreCase("HISTORICO_DE_LEVANTAMENTO")){
            result = packService.getPacksByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,
                    reportSearchParams.startDate,reportSearchParams.endDate)
        }
        if(reportSearchParams.reportType.equalsIgnoreCase("HISTORICO_DE_LEVANTAMENTO_PREP")){
            result = packService.getPacksByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,
                    reportSearchParams.startDate,reportSearchParams.endDate)
        }
        if(reportSearchParams.reportType.equalsIgnoreCase("HISTORICO_DE_LEVANTAMENTO_TPT")){
            result = packService.getPacksByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,
                    reportSearchParams.startDate,reportSearchParams.endDate)
        }

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100 / result.size()

            for (item in result) {
                HistoricoLevantamentoReport historicoLevantamentoReport = setGenericInfo(reportSearchParams, clinic, item[4])
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                reportProcessMonitorService.save(processMonitor)
                generateAndSaveHistory(item as List, reportSearchParams, historicoLevantamentoReport)
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

            return result
        } else {
            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)
            return new ArrayList<ActivePatientReport>()
        }

    }

    private HistoricoLevantamentoReport setGenericInfo(ReportSearchParams searchParams, Clinic clinic, Double age) {
        HistoricoLevantamentoReport historicoLevantamentoReport = new HistoricoLevantamentoReport()
        historicoLevantamentoReport.setClinic(clinic.getClinicName())
        historicoLevantamentoReport.setStartDate(searchParams.startDate)
        historicoLevantamentoReport.setEndDate(searchParams.endDate)
        historicoLevantamentoReport.setReportId(searchParams.id)
        historicoLevantamentoReport.setPeriodType(searchParams.periodType)
        historicoLevantamentoReport.setReportId(searchParams.id)
        historicoLevantamentoReport.setYear(searchParams.year)
        historicoLevantamentoReport.setAge(age!= null ? age.intValue() as String : 0 as String)
        historicoLevantamentoReport.setClinicalService(ClinicalService.findById(searchParams.clinicalService).code)
        def province = Province.findById(clinic.province.id.toString())
        province == null? historicoLevantamentoReport.setProvince(" "):historicoLevantamentoReport.setProvince(province.description)
        historicoLevantamentoReport.setProvince(province.description)
        def district = District.findById(clinic.district.id)
        district == null? historicoLevantamentoReport.setDistrict(""):historicoLevantamentoReport.setDistrict(district.description)
        return historicoLevantamentoReport
    }

    void generateAndSaveHistory(List item, ReportSearchParams reportSearchParams, HistoricoLevantamentoReport historicoLevantamentoReport) {

        item[0] == null? historicoLevantamentoReport.setNid("") : historicoLevantamentoReport.setNid(item[0].toString())
        item[1] == null  || (String.valueOf(item[1])).equalsIgnoreCase("null") ? historicoLevantamentoReport.setFirstNames("") : historicoLevantamentoReport.setFirstNames(item[1].toString())
        item[2] == null || (String.valueOf(item[2])).equalsIgnoreCase("null") ? historicoLevantamentoReport.setMiddleNames("") : historicoLevantamentoReport.setMiddleNames(item[2].toString())
        item[3] == null || (String.valueOf(item[3])).equalsIgnoreCase("null") ? historicoLevantamentoReport.setLastNames("") : historicoLevantamentoReport.setLastNames(item[3].toString())
        item[5] == null? historicoLevantamentoReport.setCellphone(" ") : historicoLevantamentoReport.setCellphone(item[5].toString())
        item[6] == null? historicoLevantamentoReport.setTipoTarv(" ") : historicoLevantamentoReport.setTipoTarv(item[6].toString())
      //  item[6] == null? historicoLevantamentoReport.setStartReason("") : historicoLevantamentoReport.setStartReason(item[6].toString())
        item[7] == null? historicoLevantamentoReport.setTherapeuticalRegimen("") : historicoLevantamentoReport.setTherapeuticalRegimen(item[7].toString())
        item[8] == null? historicoLevantamentoReport.setDispenseType("") : historicoLevantamentoReport.setDispenseType(item[8].toString())
        item[9] == null? historicoLevantamentoReport.setDispenseMode("") : historicoLevantamentoReport.setDispenseMode(item[9].toString())
        item[15] == null? historicoLevantamentoReport.setClinicsector("") : historicoLevantamentoReport.setClinicsector(item[15].toString())


        // set pickUpDate
        if (item[10] != null) {
            Date pickUpDate = formatter.parse(item[10].toString())
            historicoLevantamentoReport.setPickUpDate(pickUpDate)
        }
        // set nextPickUpDate
        if (item[11] != null) {
            Date nextPickUpDate = formatter.parse(item[11].toString())
            historicoLevantamentoReport.setNexPickUpDate(nextPickUpDate)
        }
        save(historicoLevantamentoReport)
    }
}
