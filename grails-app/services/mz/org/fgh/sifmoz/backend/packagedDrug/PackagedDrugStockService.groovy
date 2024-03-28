package mz.org.fgh.sifmoz.backend.packagedDrug

import grails.gorm.services.Service

@Service(PackagedDrugStock)
interface PackagedDrugStockService {
    PackagedDrugStock get(Serializable id)

    List<PackagedDrugStock> list(Map args)

    Long count()

    PackagedDrugStock delete(Serializable id)

    PackagedDrugStock save(PackagedDrugStock packagedDrug)
}
