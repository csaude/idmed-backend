package mz.org.fgh.sifmoz.backend.reports.patients

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Transactional
@Service(ActivePatientReport)
abstract class ActivePatientReportService implements IActivePatientReportService {
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IPackService packService

    @Autowired
    SessionFactory sessionFactory

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Override
    ActivePatientReport get(Serializable id) {
        return ActivePatientReport.findById(id as String)
    }

    @Override
    List<ActivePatientReport> list(Map args) {
        return null
    }

    @Override
    Long count() {
        return null
    }

    @Override
    ActivePatientReport delete(Serializable id) {
        return null
    }

    @Override
    void doSave(List<ActivePatientReport> patients) {
        for (patient in patients) {
            save(patient)
        }
    }

    @Override
    List<ActivePatientReport> getReportDataByReportId(String reportId) {
        def res = ActivePatientReport.findAllByReportId(reportId)
        return res
    }

    @Override
    List<ActivePatientReport> processamentoDados(ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor) {

        List<ActivePatientReport> resultList = new ArrayList<>()
//
        String reportId = reportSearchParams.getId()
        //---------------
    //    String clinicalService = ClinicalService.findById(reportSearchParams.getClinicalService()).code
        Clinic clinic = Clinic.findById(reportSearchParams.clinicId)

        def result

        // NOVOS REPORTS [Para nao criar controllers novos]
        if(reportSearchParams.reportType.equalsIgnoreCase("PACIENTES_EM_DS")){
            result = packService.getPacientesEmDispensaSemensal(reportSearchParams)
        } else if(reportSearchParams.reportType.equalsIgnoreCase("PACIENTES_EM_DT")){
            result = packService.getPacientesEmDispensaTrimestral(reportSearchParams)
        } else {
            result = packService.getActivePatientsReportDataByReportParams(reportSearchParams)
        }

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100.0 / result.size()

            for (item in result) {
                ActivePatientReport activePatientReport = setGenericInfo(reportSearchParams, clinic, item[4] as Date)
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                generateAndSaveActivePacient(item as List, activePatientReport, reportId, reportSearchParams)
                resultList.add(activePatientReport)
                if (processMonitor.getProgress() > 99.6) processMonitor.setProgress(100)
                ReportProcessMonitor.withNewTransaction {
                    processMonitor.save(flush:true)
                }
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

            return resultList
        } else {
            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)
            return new ArrayList<ActivePatientReport>()
        }
    }


    private ActivePatientReport setGenericInfo(ReportSearchParams searchParams, Clinic clinic, Date dateOfBirth) {
        ActivePatientReport activePatientReport = new ActivePatientReport()
        activePatientReport.setClinic(clinic.getClinicName())
        activePatientReport.setStartDate(searchParams.startDate)
        activePatientReport.setEndDate(searchParams.endDate)
        activePatientReport.setReportId(searchParams.id)
        activePatientReport.setPeriodType(searchParams.periodType)
        activePatientReport.setReportId(searchParams.id)
        activePatientReport.setYear(searchParams.year)
        activePatientReport.setAge(ConvertDateUtils.getAge(dateOfBirth).intValue() as String)

//        activePatientReport.setProvince(Province.findById(searchParams.provinceId).description)
        activePatientReport.setProvince(Province.findById(clinic.province.id).description)
        clinic.district == null? activePatientReport.setDistrict("") : activePatientReport.setDistrict(District.findById(clinic.district.id).description)
        return activePatientReport
    }

    void generateAndSaveActivePacient(List item, ActivePatientReport activePatientReport, String reportId, ReportSearchParams searchParams) {

        activePatientReport.setReportId(reportId)
        item[0].toString() == null || (item[0].toString()).equalsIgnoreCase("null") ? activePatientReport.setFirstNames("") : activePatientReport.setFirstNames(item[0].toString())
        item[1].toString() == null || (item[1].toString()).equalsIgnoreCase("null") ? activePatientReport.setMiddleNames("") : activePatientReport.setMiddleNames(item[1].toString())
        item[2].toString() == null || (item[2].toString()).equalsIgnoreCase("null") ? activePatientReport.setLastNames("") : activePatientReport.setLastNames(item[2].toString())
        activePatientReport.setGender(item[3].toString())
        item[5] == null ? activePatientReport.setCellphone("") : activePatientReport.setCellphone(item[5].toString())
        activePatientReport.setNid(item[11].toString())
            Date pickUpDate = formatter.parse(item[6].toString())
            activePatientReport.setPickupDate(pickUpDate)
            Date nextPickUpDate = formatter.parse(item[7].toString())
            activePatientReport.setNextPickUpDate(nextPickUpDate)
        activePatientReport.setTherapeuticRegimen(item[9].toString())
        activePatientReport.setTherapeuticLine(item[8].toString())
            activePatientReport.setPatientType(item[10].toString())

        if(searchParams.reportType.equalsIgnoreCase("PACIENTES_EM_DS") || searchParams.reportType.equalsIgnoreCase("PACIENTES_EM_DT")){
            Date prescriptionDate = formatter.parse(item[12].toString())
            activePatientReport.setPrescriptionDate(prescriptionDate)
        }

        try {
            save(activePatientReport)
        } catch (Exception e){
            println(e.message)
        }
    }

}
