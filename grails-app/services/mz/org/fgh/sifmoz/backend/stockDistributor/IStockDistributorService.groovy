package mz.org.fgh.sifmoz.backend.stockDistributor

interface IStockDistributorService {

    StockDistributor get(Serializable id)

    List<StockDistributor> list(Map args)

    Long count()

    StockDistributor delete(Serializable id)

    StockDistributor save(StockDistributor stockDistributor)

    List<StockDistributor> getAllByClinicId(String clinicId, int offset, int max)

}
