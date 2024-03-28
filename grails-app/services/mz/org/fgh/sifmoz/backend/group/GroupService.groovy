package mz.org.fgh.sifmoz.backend.group

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.service.ClinicalService

@Transactional
@Service(GroupInfo)
abstract class GroupService implements IGroupService{

    @Override
    List<GroupInfo> getAllByClinicId(String clinicId, int offset, int max) {
        return GroupInfo.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }

    @Override
    Set<GroupInfo> getAllActiveOfPatientOnService(Patient patient, String serviceCode) {
        def list = GroupInfo.executeQuery("select g " +
                                                "from GroupInfo g " +
                                                "inner join g.members gm " +
                                                "where gm.patient = :patient and g.service = :service and g.endDate is null and gm.endDate is null",
                                                [patient: patient, service: ClinicalService.findByCode(serviceCode)])

        return list
    }
}
