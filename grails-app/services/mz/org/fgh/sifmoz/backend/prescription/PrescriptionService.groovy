package mz.org.fgh.sifmoz.backend.prescription

import grails.converters.JSON
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.pocPrescriptionLog.PocPrescriptionLog
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(Prescription)
abstract class PrescriptionService implements IPrescriptionService {

    @Autowired
    SessionFactory sessionFactory

    @Override
    List<Prescription> getAllLastPrescriptionOfClinic(String clinicId, int offset, int max) {
        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from patient_last_prescription_vw  " +
                "where clinic_id = :clinic offset :offset limit :max "


        def query = session.createSQLQuery(queryString).addEntity(Prescription.class)
        query.setParameter("clinic", clinicId)
        query.setParameter("offset", offset)
        query.setParameter("max", max)
        List<Prescription> result = query.list()
        return result
    }

    @Override
    List<Prescription> getAllByClinicId(String clinicId, int offset, int max) {
        return Prescription.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

    @Override
    Prescription getByVisitIds(String pvdsId, int offset, int max) {
        // return Prescription.findAllByPatientVisitDetails(PatientVisitDetails.findById(pvdsId),[offset: offset, max: max])
        def prescription = Prescription.findByPatientVisitDetails(PatientVisitDetails.findById(pvdsId))
        System.out.println(prescription)
        return prescription
    }

    @Override
    Map<String, Prescription> getLastPrescriptionsByClinicAndClinicalService(Clinic clinic, ClinicalService clinicalService) {
        List<Prescription> prescriptions = Prescription.executeQuery("select pk from Prescription pk " +
                "inner join pk.patientVisitDetails as pvd " +
                "inner join pvd.patientVisit as pv " +
                "inner join pvd.episode as ep " +
                "inner join ep.patientServiceIdentifier as psi " +
                "inner join psi.patient as p " +
                "inner join psi.service as s " +
                "inner join ep.clinic c " +
                "where c.id= ?0 and s.id = ?1 and pk.prescriptionDate = (select max(pk2.prescriptionDate) from Prescription pk2 " +
                "inner join pk2.patientVisitDetails as pvd2 " +
                "inner join pvd2.patientVisit as pv2 " +
                "inner join pvd2.episode as ep2 " +
                "inner join ep2.patientServiceIdentifier as psi2 " +
                "inner join psi2.patient as p2 " +
                "inner join ep2.clinic c2 " +
                "inner join psi2.service as s2 " +
                "where p.id=p2.id and c2.id= ?0 and s2.id = ?1) order by pk.prescriptionDate desc ", [clinic.id, clinicalService.id])

        Map<String, Prescription> map = new HashMap<String, Prescription>()
        for (Prescription prescription : prescriptions) {
            map.put(prescription.getPatientVisitDetails().getAt(0).patientVisit.patient.id, prescription)
        }

        return map
    }
    //select pk from Prescription pk
    Map<String, PatientVisitDetails> getLastPrescriptionsByClinicAndClinicalServiceAndEndDate(Clinic clinic, ClinicalService clinicalService, Date endDate) {
        List<PatientVisitDetails> patientVisitDetailsList = Prescription.executeQuery("select pvd from PatientVisitDetails pvd " +
                "inner join pvd.prescription as pk " +
                "inner join pvd.patientVisit as pv " +
                "inner join pvd.episode as ep " +
                "inner join ep.patientServiceIdentifier as psi " +
                "inner join psi.patient as p " +
                "inner join psi.service as s " +
                "inner join pvd.clinic c " +
                "where c.id= ?0 and s.id = ?1 and pk.prescriptionDate < ?2 and pk.prescriptionDate = (select max(pk2.prescriptionDate) from PatientVisitDetails pvd2 " +
                "inner join pvd2.prescription as pk2 " +
                "inner join pvd2.patientVisit as pv2 " +
                "inner join pvd2.episode as ep2 " +
                "inner join ep2.patientServiceIdentifier as psi2 " +
                "inner join psi2.patient as p2 " +
                "inner join pvd2.clinic c2 " +
                "inner join psi2.service as s2 " +
                "where p.id=p2.id and c2.id= ?0 and s2.id = ?1 and pk2.prescriptionDate < ?2) order by pk.prescriptionDate desc ", [clinic.id, clinicalService.id, endDate])

        Map<String, PatientVisitDetails> map = new HashMap<String, PatientVisitDetails>()
        for (PatientVisitDetails patientVisitDetail : patientVisitDetailsList) {
            map.put(patientVisitDetail.patientVisit.patient.id, patientVisitDetail)
        }

        return map
    }

    Prescription getLastPrescriptionByPatientId(String patientId) {
        def patient = Patient.get(patientId)
        def lastPatientVisit = PatientVisit.findAllByPatient(patient)
        def patientVisitDetails = PatientVisitDetails.findAllByPatientVisitInList(lastPatientVisit)
        def prescriptions = Prescription.findAllByIdInList(patientVisitDetails?.prescription?.id,
                [sort: "prescriptionDate", order: "desc"])

        return prescriptions.size() > 0 ? prescriptions.get(0) : null
    }



    Prescription getLastPrescriptionWithoutDetailsByPatientIdAndClinicalServiceId(String patientId,String clinicalServiceId) {
        def patient = Patient.get(patientId)
        def clinicalService = ClinicalService.get(clinicalServiceId)
       PocPrescriptionLog pocPrescriptionLog = PocPrescriptionLog.findAllByPatientAndClinicalService(patient,clinicalService, [sort: "prescriptionDate", order: "desc"])?.first()

        return  pocPrescriptionLog.getPrescription()
    }

    List<Prescription> getAllPrescriptionFromPocByPatientId(String patientId) {
        def patient = Patient.get(patientId)
       def pocPrescriptions=  PocPrescriptionLog.findAllByPatient(patient, [sort: "prescriptionDate", order: "desc"])

        def prescriptions = []
        pocPrescriptions.each { log ->
            prescriptions << log.prescription
        }
        return pocPrescriptions

    }


    void savePrescriptionFromPOC(def objectJSON, String messageId, Patient patient) {
        try {
            Prescription prescription = Prescription.findWhere(id: messageId)
            if (!objectJSON?.empty && !objectJSON) {
                if (!prescription) {
                    prescription = new Prescription()
                    prescription.beforeInsert()
                }
                prescription.prescriptionDate = objectJSON.prescriptionDate
                prescription.expiryDate = objectJSON.expiryDate
                prescription.current = objectJSON.current
                prescription.notes = objectJSON.notes
                prescription.patientType = objectJSON.patientType
                prescription.doctor = objectJSON.doctor
                prescription.duration = objectJSON.duration
                prescription.origin = prescription?.clinic?.id

                addPrescriptionDetails(prescription, objectJSON)
                addPrescribedDrugs(prescription, objectJSON)

                if(prescription.save(flush: true))
                    savePOCPrescriptionLog(prescription, objectJSON, patient)
            }
        } catch (Exception e) {
            e.printStackTrace()
        }

    }

    void addPrescriptionDetails(Prescription prescription, def objectJSON) {
        PrescriptionDetail prescriptionDetail = PrescriptionDetail.findWhere(prescription: prescription)
        if(!prescriptionDetail){
            prescriptionDetail = new PrescriptionDetail()
            prescriptionDetail.beforeInsert()
        }
//      prescriptionDetail.reasonForUpdate
//      prescriptionDetail.reasonForUpdateDesc
        prescriptionDetail.therapeuticLine = objectJSON.therapeuticLine
        prescriptionDetail.therapeuticRegimen = objectJSON.therapeuticRegimen
        prescriptionDetail.dispenseType = objectJSON.dispenseType
        prescriptionDetail.prescription = prescription
        prescriptionDetail.spetialPrescriptionMotive = objectJSON.spetialPrescriptionMotive
        prescriptionDetail.origin = prescription.origin
        prescription.addToPrescriptionDetails(prescriptionDetail)

    }

    void addPrescribedDrugs(Prescription prescription, def objectJSON) {
        PrescribedDrug prescribedDrug = PrescribedDrug.findWhere(prescription: prescription)
        if(!prescribedDrug){
            prescribedDrug = new PrescribedDrug()
            prescribedDrug.beforeInsert()
        }

        prescribedDrug.amtPerTime = objectJSON.amtPerTime
        prescribedDrug.timesPerDay = objectJSON.timesPerDay
        prescribedDrug.prescribedQty = objectJSON.prescribedQty
        prescribedDrug.form = objectJSON.form
        prescribedDrug.drug = objectJSON.drug
        prescribedDrug.prescription = prescription
        prescribedDrug.origin = prescription.origin
        prescription.addToPrescribedDrugs(prescribedDrug)

    }

    void savePOCPrescriptionLog(Prescription prescription, def objectJSON, patient) {
        PocPrescriptionLog pocPrescriptionLog = PocPrescriptionLog.findWhere(prescription: prescription)

        if(!pocPrescriptionLog){
            pocPrescriptionLog = new PocPrescriptionLog()
            pocPrescriptionLog.beforeInsert()
        }
        pocPrescriptionLog.messageId = prescription.id
        pocPrescriptionLog.prescriptionDate = prescription.prescriptionDate
        pocPrescriptionLog.clinicalService = objectJSON.clinicalService
        pocPrescriptionLog.patient = patient
        pocPrescriptionLog.nid = objectJSON.nid
        pocPrescriptionLog.prescription = prescription
        pocPrescriptionLog.status = "PENDING"
        pocPrescriptionLog.save(flush: true)

    }

    void updatePOCPrescriptionLog(String messageId) {
        PocPrescriptionLog pocPrescriptionLog = PocPrescriptionLog.findWhere(messageId: messageId)

        if(pocPrescriptionLog) {
            pocPrescriptionLog.status = "COMPLETED"
            pocPrescriptionLog.save(flush: true)
        }

    }
}
