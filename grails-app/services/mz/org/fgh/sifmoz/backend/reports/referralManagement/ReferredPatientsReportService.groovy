package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.EpisodeService
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.patients.ActivePatientReport
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.historicoLevantamento.HistoricoLevantamentoReport
import mz.org.fgh.sifmoz.backend.reports.referralManagement.IReferredPatientsReportService
import mz.org.fgh.sifmoz.backend.reports.referralManagement.ReferredPatientsReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat

@Transactional
@Service(ReferredPatientsReport)
abstract class ReferredPatientsReportService implements IReferredPatientsReportService{
    @Autowired
    IPrescriptionService prescriptionService
    @Autowired
    IPackService packService
    EpisodeService episodeService
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

    PatientVisitDetailsService patientVisitDetailsService

    @Override
    void processReferredAndBackReferredReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor, String refIdaOuVoltaParam ) {
        def refIdaOuVolta = refIdaOuVoltaParam

        def result = patientVisitDetailsService.getReferralPatients(searchParams.getClinicId(),
                searchParams.getStartDate(),
                searchParams.getEndDate(), refIdaOuVolta)

        Clinic clinic = Clinic.findById(searchParams.clinicId)
//        ClinicalService clinicalService = ClinicalService.findById(searchParams.clinicalService)
        Episode episode

        double percentageUnit
        if (result.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        }  else{
            percentageUnit = 100/result.size()
        }
        List<ReferredPatientsReport> referencesToCreate = new ArrayList<>()

        for (item in result) {
            List refPatient = item as List
            episode = Episode.get(refPatient[14] == null ? "": refPatient[14].toString())

            ReferredPatientsReport referredPatient = setGenericInfo(searchParams,clinic,episode)
            referredPatient.setNid(refPatient[1].toString())
            referredPatient.setName(refPatient[2].toString())
            referredPatient.setAge(Integer.parseInt(refPatient[3].toString()))
            referredPatient.setLastPrescriptionDate(refPatient[4] == null ? "": refPatient[4] as Date)
            referredPatient.setTherapeuticalRegimen(refPatient[5] == null ? "": refPatient[5].toString())
            referredPatient.setDispenseType(refPatient[7] == null ? "": refPatient[7].toString())
            referredPatient.setPickUpDate(refPatient[8] == null ? "": refPatient[8] as Date)
            referredPatient.setNextPickUpDate(refPatient[9] == null ? "": refPatient[9] as Date)
            referredPatient.setReferrenceDate(refPatient[10] == null ? "": refPatient[10] as Date)
            referredPatient.setReferralPharmacy(refPatient[11] == null ? "": refPatient[11].toString())
            referredPatient.setNotes(refPatient[12] == null ? "": refPatient[12].toString())
            referredPatient.setContact((refPatient[13] == null ? "": refPatient[13].toString()))
            referredPatient.setDateBackUs(refPatient[10] == null ? "": refPatient[10] as Date)
            referredPatient.setLastPickUpDate(refPatient[8] == null ? "": refPatient[8] as Date)
            referencesToCreate.add(referredPatient)
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
                setProcessMonitor(processMonitor)
            }
            reportProcessMonitorService.save(processMonitor)
            save(referredPatient)
        }
    }


    @Override
    void processReportReferredDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        Clinic clinic = Clinic.findById(searchParams.clinicId)
        ClinicalService clinicalService = ClinicalService.findById(searchParams.clinicalService)
        def result = packService.getReferredPatintsPacksByClinicalServiceAndClinicOnPeriod(clinicalService,clinic,searchParams.startDate,searchParams.endDate)

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100 / result.size()

            for (item in result) {
                ReferredPatientsReport historicoLevantamentoReport = setGenericInfo1(searchParams, clinic, item[4])
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                reportProcessMonitorService.save(processMonitor)
                generateAndSaveHistory(item as List, searchParams, historicoLevantamentoReport)
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

        } else {
            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)
        }

    }

    @Override
    void processReportAbsentReferredDispenseRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        Clinic clinic = Clinic.findById(searchParams.clinicId)
        List absentReferredPatients = patientVisitDetailsService.getAbsentReferredPatientsByClinicalServiceAndClinicOnPeriod(clinic.id,searchParams.startDate,searchParams.endDate)
        double percentageUnit
        if (absentReferredPatients.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        }  else{
            percentageUnit = 100/absentReferredPatients.size()
        }

            Episode episode
        for (item in absentReferredPatients) {
            List refPatient = item as List
            episode = Episode.get(refPatient[14] == null ? "": refPatient[14].toString())

            ReferredPatientsReport referredPatient = setGenericInfo(searchParams,clinic,episode)
            referredPatient.setNid(refPatient[1].toString())
            referredPatient.setName(refPatient[2].toString())
            referredPatient.setAge(Integer.parseInt(refPatient[3].toString()))
            referredPatient.setLastPrescriptionDate(refPatient[4] == null ? "": refPatient[4] as Date)
            referredPatient.setTherapeuticalRegimen(refPatient[5] == null ? "": refPatient[5].toString())
            referredPatient.setDispenseType(refPatient[7] == null ? "": refPatient[7].toString())
            referredPatient.setPickUpDate(refPatient[8] == null ? "": refPatient[8] as Date)
            referredPatient.setNextPickUpDate(refPatient[9] == null ? "": refPatient[9] as Date)
            referredPatient.setReferrenceDate(refPatient[10] == null ? "": refPatient[10] as Date)
            referredPatient.setReferralPharmacy(refPatient[11] == null ? "": refPatient[11].toString())
            referredPatient.setNotes(refPatient[12] == null ? "": refPatient[12].toString())
            referredPatient.setContact((refPatient[13] == null ? "": refPatient[13].toString()))
            referredPatient.setDateBackUs(refPatient[0] == null ? "": refPatient[0] as Date)
            referredPatient.setLastPickUpDate(refPatient[8] == null ? "": refPatient[8] as Date)
            referredPatient.setDateMissedPickUp(refPatient[9] == null ? "": refPatient[9] as Date)
            if (refPatient[15] != null) {
                referredPatient.setReturnedPickUp(refPatient[15] as Date)
                Date abandonmentDate = ConvertDateUtils.addDaysDate(referredPatient.dateMissedPickUp,60)
                if(searchParams.endDate.after(abandonmentDate)) {
                    referredPatient.setDateIdentifiedAbandonment(abandonmentDate)
                }
            }

            save(referredPatient)
            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
                setProcessMonitor(processMonitor)
            }
            reportProcessMonitorService.save(processMonitor)
        }

    }

    private ReferredPatientsReport setGenericInfo(ReportSearchParams searchParams,Clinic clinic,Episode episode) {

        ReferredPatientsReport referredPatient = new ReferredPatientsReport()
        referredPatient.setPharmacyId(clinic.id)
        referredPatient.setStartDate(searchParams.startDate)
        referredPatient.setEndDate(searchParams.endDate)
        referredPatient.setClinicalServiceId(episode.patientServiceIdentifier.service.code)
        referredPatient.setReportId(searchParams.id)
        referredPatient.setPeriodType(searchParams.periodType)
        referredPatient.setReportId(searchParams.id)
        referredPatient.setAge(ConvertDateUtils.getAge(episode.patientServiceIdentifier.patient.dateOfBirth).intValue())
        referredPatient.setNid(episode.patientServiceIdentifier.value)
        def firstName = String.valueOf(episode.patientServiceIdentifier.patient.firstNames) == null || (String.valueOf(episode.patientServiceIdentifier.patient.firstNames)).equalsIgnoreCase("null") ? "" : String.valueOf(episode.patientServiceIdentifier.patient.firstNames)
        def lastName = String.valueOf(episode.patientServiceIdentifier.patient.lastNames) == null || (String.valueOf(episode.patientServiceIdentifier.patient.lastNames)).equalsIgnoreCase("null") ? "" : String.valueOf(episode.patientServiceIdentifier.patient.lastNames)
        referredPatient.setName(firstName +' '+lastName)
        return referredPatient
    }

    private ReferredPatientsReport setGenericInfo1(ReportSearchParams searchParams, Clinic clinic, Double age) {
        ReferredPatientsReport historicoLevantamentoReport = new ReferredPatientsReport()
        historicoLevantamentoReport.setStartDate(searchParams.startDate)
        historicoLevantamentoReport.setEndDate(searchParams.endDate)
        historicoLevantamentoReport.setReportId(searchParams.id)
        historicoLevantamentoReport.setPeriodType(searchParams.periodType)
        historicoLevantamentoReport.setReportId(searchParams.id)
        historicoLevantamentoReport.setAge(age!= null ? age.intValue() : 0 )
        historicoLevantamentoReport.setClinicalServiceId(ClinicalService.findByCode('TARV').id)
        def province = Province.findById(clinic.province.id.toString())
        province == null? historicoLevantamentoReport.setProvinceId(" "):historicoLevantamentoReport.setProvinceId(province.id)
        historicoLevantamentoReport.setProvinceId(province.id)
        def district = District.findById(clinic.district.id)
        district == null? historicoLevantamentoReport.setDistrictId(""):historicoLevantamentoReport.setDistrictId(district.id)
        return historicoLevantamentoReport
    }

    void generateAndSaveHistory(List item, ReportSearchParams reportSearchParams, ReferredPatientsReport historicoLevantamentoReport) {

        item[0] == null? historicoLevantamentoReport.setNid("") : historicoLevantamentoReport.setNid(item[0].toString())
        item[1] == null  || (String.valueOf(item[1])).equalsIgnoreCase("null") ? historicoLevantamentoReport.setName("") : historicoLevantamentoReport.setName(item[1].toString()+" "+item[2].toString()+" "+item[3].toString())
        item[7] == null? historicoLevantamentoReport.setTherapeuticalRegimen("") : historicoLevantamentoReport.setTherapeuticalRegimen(item[7].toString())
        item[8] == null? historicoLevantamentoReport.setDispenseType("") : historicoLevantamentoReport.setDispenseType(item[8].toString())

        // set pickUpDate
        if (item[10] != null) {
            Date pickUpDate = formatter.parse(item[10].toString())
            historicoLevantamentoReport.setPickUpDate(pickUpDate)
        }
        // set nextPickUpDate
        if (item[11] != null) {
            Date nextPickUpDate = formatter.parse(item[11].toString())
            historicoLevantamentoReport.setNextPickUpDate(nextPickUpDate)
        }
        save(historicoLevantamentoReport)
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }
}
