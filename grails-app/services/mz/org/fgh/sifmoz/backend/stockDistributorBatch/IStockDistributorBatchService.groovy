package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockDistributor.StockDistributor

interface IStockDistributorBatchService {

    StockDistributorBatch get(Serializable id)

    List<StockDistributorBatch> list(Map args)

    Long count()

    StockDistributorBatch delete(Serializable id)

    StockDistributorBatch save(StockDistributorBatch stock)

    List<StockDistributorBatch> getStockDistributorBatchByDrugDistributorId(String idDdrugDistributor)


}
