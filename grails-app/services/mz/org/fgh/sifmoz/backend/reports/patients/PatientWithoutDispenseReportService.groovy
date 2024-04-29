package mz.org.fgh.sifmoz.backend.reports.patients

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Transactional
@Service(PatientWithoutDispenseReport)
abstract class PatientWithoutDispenseReportService implements IPatientWithoutDispenseReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IPatientService patientService

    @Autowired
    SessionFactory sessionFactory

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Override
    PatientWithoutDispenseReport get(Serializable id) {
        return ActivePatientReport.findById(id as String)
    }

    @Override
    List<PatientWithoutDispenseReport> list(Map args) {
        return null
    }

    @Override
    Long count() {
        return null
    }

    @Override
    PatientWithoutDispenseReport delete(Serializable id) {
        return null
    }

    @Override
    void doSave(List<PatientWithoutDispenseReport> patients) {
        for (patient in patients) {
            save(patient)
        }
    }

    @Override
    List<PatientWithoutDispenseReport> getReportDataByReportId(String reportId) {
        def res = PatientWithoutDispenseReport.findAllByReportId(reportId)
        return res
    }

    @Override
    List<PatientWithoutDispenseReport> processamentoDados(ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor) {

        List<PatientWithoutDispenseReport> resultList = new ArrayList<>()
        String reportId = reportSearchParams.getId()
        Clinic clinic = Clinic.findById(reportSearchParams.clinicId)

        def result

        result = patientService.getPatientWithoutDispense(reportSearchParams)

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100.0 / result.size()

            for (item in result) {
                PatientWithoutDispenseReport patientWithoutDispenseReport = setGenericInfo(reportSearchParams, clinic)
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                generateAndSaveActivePacient(item as List, patientWithoutDispenseReport, reportId, reportSearchParams)
                resultList.add(patientWithoutDispenseReport)
                if (processMonitor.getProgress() > 99.6) processMonitor.setProgress(100)
                ReportProcessMonitor.withNewTransaction {
                    processMonitor.save(flush: true)
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


    private PatientWithoutDispenseReport setGenericInfo(ReportSearchParams searchParams, Clinic clinic) {
        PatientWithoutDispenseReport patientWithoutDispenseReport = new PatientWithoutDispenseReport()
        patientWithoutDispenseReport.setClinic(clinic.getClinicName())
        patientWithoutDispenseReport.setStartDate(searchParams.startDate)
        patientWithoutDispenseReport.setEndDate(searchParams.endDate)
        patientWithoutDispenseReport.setReportId(searchParams.id)
        patientWithoutDispenseReport.setPeriodType(searchParams.periodType)
        patientWithoutDispenseReport.setReportId(searchParams.id)
        patientWithoutDispenseReport.setYear(searchParams.year)
        patientWithoutDispenseReport.setProvince(Province.findById(clinic.province.id).description)
        clinic.district == null? patientWithoutDispenseReport.setDistrict("") : patientWithoutDispenseReport.setDistrict(District.findById(clinic.district.id).description)

        return patientWithoutDispenseReport
    }

    void generateAndSaveActivePacient(List item, PatientWithoutDispenseReport patientWithoutDispenseReport, String reportId, ReportSearchParams searchParams) {

        patientWithoutDispenseReport.setReportId(reportId)
        item[0].toString() == null || (item[0].toString()).equalsIgnoreCase("null") ? patientWithoutDispenseReport.setFirstNames("") : patientWithoutDispenseReport.setFirstNames(item[0].toString())
        item[1].toString() == null || (item[1].toString()).equalsIgnoreCase("null") ? patientWithoutDispenseReport.setMiddleNames("") : patientWithoutDispenseReport.setMiddleNames(item[1].toString())
        item[2].toString() == null || (item[2].toString()).equalsIgnoreCase("null") ? patientWithoutDispenseReport.setLastNames("") : patientWithoutDispenseReport.setLastNames(item[2].toString())
        patientWithoutDispenseReport.setNid(item[3])
        patientWithoutDispenseReport.setUuidOpenMrs(item[4])
        Date data =formatter.parse(item[5].toString())
        patientWithoutDispenseReport.setCreateDate(data)


        try {
            save(patientWithoutDispenseReport)
        } catch (Exception e) {
            println(e.message)
        }
    }
}
