package mz.org.fgh.sifmoz.backend.stockrefered

import grails.gorm.services.Service

@Service(ReferedStockMoviment)
interface ReferedStockMovimentService {

    ReferedStockMoviment get(Serializable id)

    List<ReferedStockMoviment> list(Map args)

    Long count()

    ReferedStockMoviment delete(Serializable id)

    ReferedStockMoviment save(ReferedStockMoviment referedStockMoviment)

}
