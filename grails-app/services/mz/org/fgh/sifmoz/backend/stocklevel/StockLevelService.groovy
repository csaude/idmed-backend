package mz.org.fgh.sifmoz.backend.stocklevel

import grails.gorm.services.Service

@Service(StockLevel)
interface StockLevelService {

    StockLevel get(Serializable id)

    List<StockLevel> list(Map args)

    Long count()

    StockLevel delete(Serializable id)

    StockLevel save(StockLevel stockLevel)

}
