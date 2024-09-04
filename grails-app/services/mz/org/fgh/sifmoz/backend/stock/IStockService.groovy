package mz.org.fgh.sifmoz.backend.stock

import grails.gorm.services.Query
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance

interface IStockService {

    Stock get(Serializable id)

    List<Stock> list(Map args)

    Long count()

    Stock delete(Serializable id)

    Stock save(Stock stock)


    @Query("select ${s} from ${Stock s} where s.units_received > 0 and s.drug_id =  ${drug.getId()}")
    List<Stock> findAllOnceReceivedByDrug(Drug drug)

    boolean validateStock(String drugId, Date dateToCompare, int qtyPrescribed, String clinicId)

    List<Stock> getValidStockByDrugAndPickUpDate(String drugId, Date dateToCompare)

    List<Stock> getValidStockByDrug(Drug drug)

    boolean existsBatchNumber(String batchNumber, String clinicId)

    Stock getStockByBatchNumberAndClinic(String batchNumber, String clinicId)

    List<Stock> getAllByClinicId(String clinicId, int offset, int max)

    List<Stock> getStocksByStockDistributor(String clinicId,int offset, int max)



}
