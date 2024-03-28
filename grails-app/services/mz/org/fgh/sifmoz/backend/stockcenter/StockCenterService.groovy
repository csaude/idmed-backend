package mz.org.fgh.sifmoz.backend.stockcenter

import grails.gorm.services.Service

@Service(StockCenter)
interface StockCenterService {

    StockCenter get(Serializable id)

    List<StockCenter> list(Map args)

    Long count()

    StockCenter delete(Serializable id)

    StockCenter save(StockCenter stockCenter)

}
