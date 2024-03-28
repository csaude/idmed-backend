package mz.org.fgh.sifmoz.backend.prescriptionDetail

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.patient.Patient


interface IPrescriptionDetailService {

    PrescriptionDetail get(Serializable id)

    List<PrescriptionDetail> list(Map args)

    Long count()

    PrescriptionDetail delete(Serializable id)

    PrescriptionDetail save(PrescriptionDetail prescriptionDetail)

    List<PrescriptionDetail> getAllByPrescriptionId(String prescriptionId)

}
