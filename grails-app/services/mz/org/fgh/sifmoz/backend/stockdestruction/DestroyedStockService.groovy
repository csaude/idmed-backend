package mz.org.fgh.sifmoz.backend.stockdestruction

import grails.gorm.services.Service

@Service(DestroyedStock)
interface DestroyedStockService {

    DestroyedStock get(Serializable id)

    List<DestroyedStock> list(Map args)

    Long count()

    DestroyedStock delete(Serializable id)

    DestroyedStock save(DestroyedStock destroyedStock)

}
