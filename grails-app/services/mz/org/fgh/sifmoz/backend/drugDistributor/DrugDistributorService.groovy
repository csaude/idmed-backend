package mz.org.fgh.sifmoz.backend.drugDistributor

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.service.ClinicalService

@Service(DrugDistributor)
interface DrugDistributorService {

    DrugDistributor get(Serializable id)

    List<DrugDistributor> list(Map args)

    Long count()

    DrugDistributor delete(Serializable id)

    DrugDistributor save(DrugDistributor drugDistributor)
}
