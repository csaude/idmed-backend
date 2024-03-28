package mz.org.fgh.sifmoz.backend.prescriptionDrug

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.patient.Patient


interface IPrescribedDrugService {

    PrescribedDrug get(Serializable id)

    List<PrescribedDrug> list(Map args)

    Long count()

    PrescribedDrug delete(Serializable id)

    PrescribedDrug save(PrescribedDrug prescribedDrug)

    List<PrescribedDrug> getAllByPrescriptionId(String prescriptionId)

}
