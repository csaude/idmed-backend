package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drugDistributor.DrugDistributor
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance

@Transactional
@Service(StockDistributorBatch)
  abstract  class StockDistributorBatchService implements IStockDistributorBatchService {

  @Override
  List<StockDistributorBatch> getStockDistributorBatchByDrugDistributorId(String idDdrugDistributor) {
    def obj = StockDistributorBatch.findAllByDrugDistributor(DrugDistributor.findById(idDdrugDistributor) )
    return obj
  }

}



