package mz.org.fgh.sifmoz.backend.reports.patients

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Transactional
@Service(ExpectedPatientReport)
abstract class ExpectedPatientReportService implements IExpectedPatientReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IPatientService patientService

    @Autowired
    SessionFactory sessionFactory

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Override
    ExpectedPatientReport get(Serializable id) {
        return ExpectedPatientReport.findById(id as String)
    }

    @Override
    List<ExpectedPatientReport> list(Map args) {
        return null
    }

    @Override
    Long count() {
        return null
    }

    @Override
    ExpectedPatientReport delete(Serializable id) {
        return null
    }

    @Override
    void doSave(List<ExpectedPatientReport> patients) {
        for (patient in patients) {
            save(patient)
        }
    }

    @Override
    List<ExpectedPatientReport> getReportDataByReportId(String reportId) {
        def res = ExpectedPatientReport.findAllByReportId(reportId)
        return res
    }

    @Override
    List<ExpectedPatientReport> processamentoDados(ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor) {

        List<ExpectedPatientReport> resultList = new ArrayList<>()
        String reportId = reportSearchParams.getId()
        Clinic clinic = Clinic.findById(reportSearchParams.clinicId)

        def result

        result = patientService.getAllExpectedPatients(reportSearchParams)

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100.0 / result.size()

            for (item in result) {
                ExpectedPatientReport expectedPatientReport = setGenericInfo(reportSearchParams, clinic)
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                generateAndSaveActivePacient(item as List, expectedPatientReport, reportId, reportSearchParams)
                resultList.add(expectedPatientReport)
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


    private ExpectedPatientReport setGenericInfo(ReportSearchParams searchParams, Clinic clinic) {
        ExpectedPatientReport expectedPatientReport = new ExpectedPatientReport()
        expectedPatientReport.setClinic(clinic.getClinicName())
        expectedPatientReport.setStartDate(searchParams.startDate)
        expectedPatientReport.setEndDate(searchParams.endDate)
        expectedPatientReport.setReportId(searchParams.id)
        expectedPatientReport.setPeriodType(searchParams.periodType)
        expectedPatientReport.setReportId(searchParams.id)
        expectedPatientReport.setYear(searchParams.year)
        expectedPatientReport.setProvince(Province.findById(clinic.province.id).description)
        clinic.district == null ? expectedPatientReport.setDistrict("") : expectedPatientReport.setDistrict(District.findById(clinic.district.id).description)

        return expectedPatientReport
    }

    void generateAndSaveActivePacient(List item, ExpectedPatientReport expectedPatientReport, String reportId, ReportSearchParams searchParams) {
        expectedPatientReport.setReportId(reportId)
        item[0].toString() == null || (item[0].toString()).equalsIgnoreCase("null") ? expectedPatientReport.setFirstNames("") : expectedPatientReport.setFirstNames(item[0].toString())
        item[1].toString() == null || (item[1].toString()).equalsIgnoreCase("null") ? expectedPatientReport.setMiddleNames("") : expectedPatientReport.setMiddleNames(item[1].toString())
        item[2].toString() == null || (item[2].toString()).equalsIgnoreCase("null") ? expectedPatientReport.setLastNames("") : expectedPatientReport.setLastNames(item[2].toString())
        expectedPatientReport.setNid(item[8])
        Date data = formatter.parse(item[4].toString())
        expectedPatientReport.setNextPickUpDate(data)
        expectedPatientReport.setTherapeuticRegimen(item[5].toString())
        expectedPatientReport.setClinic(item[7].toString())
        expectedPatientReport.setDispenseType(item[6].toString())

        try {
            save(expectedPatientReport)
        } catch (Exception e) {
            println(e.message)
        }
    }
}
