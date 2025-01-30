package mz.org.fgh.sifmoz.backend.patientVisit

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(ExternalPatientVisit)
interface ExternalPatientVisitService {

    ExternalPatientVisit get(Serializable id)

    List<ExternalPatientVisit> list(Map args)

    Long count()

    ExternalPatientVisit delete(Serializable id)

    ExternalPatientVisit save(ExternalPatientVisit externalPatientVisit)
}