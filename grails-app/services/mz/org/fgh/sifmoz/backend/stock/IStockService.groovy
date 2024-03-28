package mz.org.fgh.sifmoz.backend.stock

import grails.gorm.services.Query
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.service.ClinicalService


interface IStockService {

    Stock get(Serializable id)

    List<Stock> list(Map args)

    Long count()

    Stock delete(Serializable id)

    Stock save(Stock stock)

    @Query("select ${s} from ${Stock s} where s.units_received > 0 and s.drug_id =  ${drug.getId()}")
    List<Stock> findAllOnceReceivedByDrug(Drug drug)

    boolean validateStock(String drugId, Date dateToCompare, int qtyPrescribed)

    List<Stock> getValidStockByDrugAndPickUpDate(String drugId, Date dateToCompare)


}
