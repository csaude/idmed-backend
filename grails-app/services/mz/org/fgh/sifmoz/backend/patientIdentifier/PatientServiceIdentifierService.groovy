package mz.org.fgh.sifmoz.backend.patientIdentifier

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.service.ClinicalService

@Transactional
@Service(PatientServiceIdentifier)
abstract class PatientServiceIdentifierService implements IPatientServiceIdentifierService{
    @Override
    List<PatientServiceIdentifier> getAllByClinicId(String clinicId, int offset, int max) {
        return PatientServiceIdentifier.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }

    @Override
    List<PatientServiceIdentifier> getAllByPatientId(String patientId, int offset, int max) {
        // List<PatientServiceIdentifier> identifiers = new ArrayList<>()
        def identifiers = PatientServiceIdentifier.findAllWhere(patient: Patient.findById(patientId))
        return identifiers
    }

    @Override
    List<PatientServiceIdentifier> getAllByPatientId(String patientId) {
        def identifiers = PatientServiceIdentifier.findAllWhere(patient: Patient.findById(patientId))
        return identifiers
    }

}
