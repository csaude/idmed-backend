package mz.org.fgh.sifmoz.backend.prescriptionDetail

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.prescription.Prescription

@Transactional
@Service(PrescriptionDetail)
abstract class PrescriptionDetailService implements IPrescriptionDetailService{
    @Override
    List<PrescriptionDetail> getAllByPrescriptionId(String prescriptionId) {
        return PrescriptionDetail.findAllByPrescription(Prescription.findById(prescriptionId))
    }
}
