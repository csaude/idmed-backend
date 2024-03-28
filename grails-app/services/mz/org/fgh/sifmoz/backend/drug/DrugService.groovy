package mz.org.fgh.sifmoz.backend.drug

import grails.gorm.services.Service
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory

@Service(Drug)
interface DrugService {

    Drug get(Serializable id)

    List<Drug> list(Map args)

    Long count()

    Drug delete(Serializable id)

    Drug save(Drug drug)
}
