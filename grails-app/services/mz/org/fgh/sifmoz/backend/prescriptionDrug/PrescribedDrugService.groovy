package mz.org.fgh.sifmoz.backend.prescriptionDrug

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.prescription.Prescription

@Transactional
@Service(PrescribedDrug)
abstract class PrescribedDrugService implements IPrescribedDrugService{
    @Override
    List<PrescribedDrug> getAllByPrescriptionId(String prescriptionId) {
        return PrescribedDrug.findAllByPrescription(Prescription.findById(prescriptionId))
    }
}
