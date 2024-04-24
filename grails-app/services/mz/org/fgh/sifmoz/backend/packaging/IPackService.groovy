package mz.org.fgh.sifmoz.backend.packaging

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia.MmiaRegimenSubReport
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia.MmiaReport
import mz.org.fgh.sifmoz.backend.service.ClinicalService


interface IPackService {
    Pack get(Serializable id)

    List<Pack> list(Map args)

    Long count()

    Pack delete(Serializable id)

    Pack save(Pack pack)

    int countPacksByDispenseTypeAndServiceOnPeriod(DispenseType dispenseType, ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    List<Pack> getPacksByServiceOnPeriod(ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    MmiaReport getMMIAPatientStatisticOnPeriod(MmiaReport mmiaReport, ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    List<MmiaRegimenSubReport> getMMIARegimenStatistic(ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    List<MmiaRegimenSubReport> getMMIARegimenStatisticTB(ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    Object getMMIADispenseTypeStatisticOnPeriod(ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    int countPacksByServiceOnPeriod(ClinicalService service, Clinic clinic, Date startDate, Date endDate)

    List<Pack> getAllLastPackOfClinic(String clinicId, int offset, int max)

    List<Pack> getPacksOfReferredPatientsByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getAbsentReferredPatientsByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getAbsentPatientsByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getAbsentPatientsDTByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getAbsentPatientsDSByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getAbsentPatientsApssByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getActivePatientsReportDataByReportParams (ReportSearchParams reportSearchParams)

    List<Pack> getPacksByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    List<Pack> getReferredPatintsPacksByClinicalServiceAndClinicOnPeriod(ClinicalService clinicalService,Clinic clinic,Date startDate, Date endDate)

    /* NOVOS REPORTS [Para nao criar controllers novos"*/
    List<Pack> getPacientesEmDispensaSemensal(ReportSearchParams reportSearchParams)
    List<Pack> getPacientesEmDispensaTrimestral (ReportSearchParams reportSearchParams)
}
