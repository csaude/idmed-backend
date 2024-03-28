package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Service(StockDestructionAdjustment)
interface StockDestructionAdjustmentService {

    StockReferenceAdjustment get(Serializable id)

    List<StockReferenceAdjustment> list(Map args)

    Long count()

    StockReferenceAdjustment delete(Serializable id)

    StockReferenceAdjustment save(StockReferenceAdjustment stockReferenceAdjustment)

}
