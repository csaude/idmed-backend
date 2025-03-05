package mz.org.fgh.sifmoz.backend.patientUpdateOpenMrsErrorLog

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(PatientUpdateOpenMrsErrorLog)
abstract class PatientUpdateOpenMrsErrorLogControllerService implements IPatientUpdateOpenMrsErrorLogControllerService{

}
