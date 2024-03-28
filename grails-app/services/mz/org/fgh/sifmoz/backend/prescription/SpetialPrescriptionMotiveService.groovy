package mz.org.fgh.sifmoz.backend.prescription

import grails.gorm.services.Service

@Service(SpetialPrescriptionMotive)
interface SpetialPrescriptionMotiveService {

    SpetialPrescriptionMotive get(Serializable id)

    List<SpetialPrescriptionMotive> list(Map args)

    Long count()

    SpetialPrescriptionMotive delete(Serializable id)

    SpetialPrescriptionMotive save(SpetialPrescriptionMotive spetialPrescriptionMotive)

}
