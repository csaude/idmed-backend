package mz.org.fgh.sifmoz.backend.patientVisit

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.IPatientService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(PatientVisit)
abstract class PatientVisitService implements IPatientVisitService{
    @Autowired
    SessionFactory sessionFactory

    @Override
    List<PatientVisit> getAllByPatientId(String patientId) {
        return PatientVisit.findAllByPatient(Patient.findById(patientId))
    }

    @Override
    List<PatientVisit> getAllByClinicId(String clinicId, int offset, int max) {
        return PatientVisit.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }

    @Override
    List<PatientVisit> getAllLastWithScreening(String clinicId, int offset, int max) {
        Session session = sessionFactory.getCurrentSession()

        String queryString ="select *  " +
                "from patient_last_visit_screening_vw  " +
                "where clinic_id = :clinic offset :offset limit :max "


        def query = session.createSQLQuery(queryString).addEntity(PatientVisit.class)
        query.setParameter("clinic", clinicId)
        query.setParameter("offset", offset)
        query.setParameter("max", max)
        List<PatientVisitDetails> result = query.list()
        return result
    }

    @Override
    PatientVisit getLastVisitOfPatient(String patientId) {
        List<PatientVisit> patientVisitList = PatientVisit.findAllByPatient(Patient.findById(patientId), [sort: ['visitDate': 'desc']])
        return patientVisitList.get(0)
    }

    List<PatientVisit> getAllLastWithScreeningByPatientIds(List<String> patientIds) {
        Session session = sessionFactory.getCurrentSession()

        String queryString ="select *  " +
                "from patient_last_visit_screening_vw  " +
                "where patient_id in (:patientIds)"


        def query = session.createSQLQuery(queryString).addEntity(PatientVisit.class)
        query.setParameter("patientIds", patientIds)
        List<PatientVisitDetails> result = query.list()
        return result
    }
}
