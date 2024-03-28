package mz.org.fgh.sifmoz.backend.clinicSector

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.IPatientVisitService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit

@Transactional
@Service(ClinicSector)
abstract class ClinicSectorService implements IClinicSectorService{

    @Override
    List<ClinicSector> getAllByClinicId(String clinicId, int offset, int max) {
        return ClinicSector.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }
}
