package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(StockDistributorBatch)
  abstract  class StockDistributorBatchService implements IStockDistributorBatchService {

  @Override
  List<StockDistributorBatch> getStockDistributorBatchByStockDistributorId(String stockDistributorId) {
    def obj = StockDistributorBatch.findAllByStockDistributor(mz.org.fgh.sifmoz.backend.stockDistributor.StockDistributor.findById(stockDistributorId) )
    return obj
  }
    }



