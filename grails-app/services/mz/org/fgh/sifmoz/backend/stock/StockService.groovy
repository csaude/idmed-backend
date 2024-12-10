package mz.org.fgh.sifmoz.backend.stock

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.stockDistributor.StockDistributor
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.apache.commons.lang3.time.DateUtils

import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
@Service(Stock)
abstract class StockService implements IStockService {

    List<Stock> getReceivedStock(String clinicId, Date startDate, Date endDate, String clinicalServiceId) {
        def startOfDay = { date -> DateUtils.truncate(date, Calendar.DATE) }

        // Create a Calendar instance to add a day to the endDate
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(startOfDay(endDate))
        calendar.add(Calendar.DATE, 1)

        List<Stock> list = Stock.executeQuery(
                "select s from Stock s " +
                        "inner join s.entrance se " +
                        "left join s.drug d " +
                        "where se.dateReceived BETWEEN :startDate AND :endDate AND " +
                        "s.clinic.id = :clinicId AND d.clinicalService.id = :clinicalServiceId",
                [
                        startDate: startOfDay(startDate),
                        endDate: startOfDay(calendar.getTime()), // Add one day to include the entire endDate
                        clinicId: clinicId,
                        clinicalServiceId: clinicalServiceId
                ]
        )

        return list
    }

    boolean validateStock(String drugId, Date dateToCompare, int qtyPrescribed, String clinicId, int weeks) {
        Drug drug = Drug.findById(drugId)
        Clinic clinic = Clinic.findById(clinicId)
        int lostDays = (int) ((weeks / 4) * 2)
        int daysToAdd = (weeks * 7 ) + lostDays

        List<Stock> list = Stock.executeQuery("select  s from Stock  s " +
                " where s.expireDate > :prescriptionDate  AND " +
                " s.stockMoviment > 0  AND s.drug = :drug AND s.clinic =:clinic ",
                [drug: drug, prescriptionDate:  Utilities.addDaysInDate(dateToCompare, daysToAdd),clinic: clinic])

        if (list.size() > 0) {
            int qtyInStock = 0
            for (Stock stock in list) {
                qtyInStock = qtyInStock + stock.stockMoviment;
            }
            if (qtyInStock < qtyPrescribed) {
                return false;
            } else {
                return true;
            }
        } else {
            return false
        }
    }


    List<Stock> getValidStockByDrugAndPickUpDate(String drugId, Date dateToCompare) {
        Drug drug = Drug.findById(drugId)
        List<Stock> list = Stock.executeQuery("select  s from Stock  s " +
                " where s.expireDate > :prescriptionDate  AND " +
                " s.stockMoviment > 0  AND s.drug = :drug order by s.expireDate asc",
                [drug: drug, prescriptionDate: dateToCompare]);

        return list;
    }


    List<Stock> getStocksByStockDistributor(String clinicId, int offset, int max) {
        Clinic clinic = Clinic.findById(clinicId)
        List<Stock> list = Stock.executeQuery("select distinct sd from StockDistributor  s " +
                " inner join s.drugDistributors  sdb " +
                " inner join sdb.stockDistributorBatchs  dd " +
                " inner join dd.stock  sd" +
                " where sdb.clinic =:clinic ",
               [clinic: clinic,max: max, offset: offset]
                );
        return list;

    }


    List<Stock> getValidStockByDrug(Drug drug) {
        List<Stock> list = Stock.executeQuery("select  s from Stock  s " +
                " where  s.expireDate > current_timestamp() and s.stockMoviment > 0  AND s.drug = :drug order by s.expireDate asc",
                [drug: drug ]);

        return list;
    }

    List getUsedStockReportRecords(ReportSearchParams searchParams, DataSource dataSource) {
        Clinic clinic = Clinic.findById(searchParams.getClinicId())
        ClinicalService service = ClinicalService.findByCode('TARV')

        String query = "SELECT \n" +
                "mmiareport.packSize,\n" +
                "mmiareport.fnmCode,\n" +
                "mmiareport.drugName,\n" +
                "mmiareport.drugId,\n" +
                "mmiareport.h as received,\n" +
                "mmiareport.i as saidas,\n" +
                "((mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n ) as ajustes,\n" +
                "((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) as saldo,\n" +
                "mmiareport.validade,\n" +
                "(((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) + mmiareport.h - mmiareport.i + (mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n) as inventario\n" +
                "FROM\n" +
                "( SELECT \n" +
                "dr.pack_Size as packSize, \n" +
                "dr.fnm_Code as fnmCode,\n" +
                "dr.name as drugName,\n" +
                "dr.id as drugId,\n" +
                "(\n" +
                "select \n" +
                "coalesce(sum(s.units_received),0) as r \n" +
                "from stock s \n" +
                "inner join stock_entrance se on se.id = s.entrance_id \n" +
                "where se.date_received < :startDate\n" +
                "and s.drug_id = dr.id \n" +
                "and s.clinic_id = :clinic\n" +
                ") as a,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                "from stock_adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date < :startDate) \n" +
                "and rf.clinic_id = :clinic\n" +
                ") as b,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from stock_adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id\n" +
                "inner join inventory i on i.id = sa.inventory_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                "and s.drug_id = dr.id \n" +
                "and (i.end_date < :startDate) \n" +
                "and i.clinic_id = :clinic\n" +
                ") as c,\n" +
                "(\n" +
                " select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_date < :startDate \n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as d,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                "from stock_adjustment sa\n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date < :startDate) \n" +
                "and rf.clinic_id = :clinic\n" +
                ") as e,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from Stock_Adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id\n" +
                "inner join inventory i on i.id = sa.inventory_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                "and s.drug_id = dr.id \n" +
                "and (i.end_date < :startDate) \n" +
                "and i.clinic_id = :clinic\n" +
                ") as f,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from Stock_Adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date < :startDate) \n" +
                "and rf.clinic_id = :clinic\n" +
                ") as g,\n" +
                "(\n" +
                "select \n" +
                "coalesce(sum(s.units_received),0) as r \n" +
                "from stock s \n" +
                "inner join stock_entrance se on se.id = s.entrance_id \n" +
                "where se.date_received >= :startDate \n" +
                "and se.date_received <= :endDate \n" +
                "and s.drug_id = dr.id \n" +
                "and s.clinic_id = :clinic\n" +
                ") as h,\n" +
                "(\n" +
                "select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_Date >= :startDate \n" +
                " and pk.pickup_Date <= :endDate\n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as i,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                "from stock_adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date >= :startDate and rf.date <= :endDate) \n" +
                "and rf.clinic_id = :clinic\n" +
                ") as j,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from stock_adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id\n" +
                "inner join inventory i on i.id = sa.inventory_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                "and s.drug_id = dr.id \n" +
                "and (i.end_date >= :startDate and i.end_date <= :endDate) \n" +
                "and i.clinic_id = :clinic\n" +
                ") as k,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                "from stock_adjustment sa\n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                "and rf.clinic_id = :clinic\n" +
                ") as l,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from Stock_Adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id\n" +
                "inner join inventory i on i.id = sa.inventory_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                "and s.drug_id = dr.id \n" +
                "and (i.end_date >= :startDate and i.end_date <= :endDate)\n" +
                "and i.clinic_id = :clinic\n" +
                ") as m,\n" +
                "(\n" +
                "select \n" +
                "coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                "from Stock_Adjustment sa \n" +
                "inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                "inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                "inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                "where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                "and s.drug_id = dr.id \n" +
                "and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                "and rf.clinic_id = :clinic\n" +
                ") as n,\n" +
                "(\n" +
                "select \n" +
                "max(s.expire_date) \n" +
                "from Stock s \n" +
                "where s.drug_id = dr.id \n" +
                "and s.clinic_id = :clinic\n" +
                ") as validade\n" +
                "\n" +
                "FROM drug dr\n" +
                "INNER JOIN Stock s ON s.drug_id = dr.id\n" +
                "WHERE  dr.active = true AND s.expire_date >= :startDate\n" +
                "and dr.clinical_service_id = :clinicalService\n" +
                ") as mmiareport\n" +
                "group by 1,2,3,4,5,6,7,8,9,10\n" +
                "ORDER BY\n" +
                "   CASE mmiareport.fnmCode\n" +
                "   WHEN '08S18WI' THEN 1\n" +
                "   WHEN '08S18W' THEN 2\n" +
                "   WHEN '08S18WII' THEN 3\n" +
                "   WHEN '08S18XI' THEN 4\n" +
                "   WHEN '08S18X' THEN 5\n" +
                "   WHEN '08S18XII' THEN 6\n" +
                "   WHEN '08S18Z' THEN 7\n" +
                "   WHEN '08S01ZY' THEN 8\n" +
                "   WHEN '08S01ZZ' THEN 9\n" +
                "   WHEN '08S30WZ' THEN 10\n" +
                "   WHEN '08S30ZY' THEN 11\n" +
                "   WHEN '08S39Z' THEN 12\n" +
                "   WHEN '08S30ZX' THEN 13\n" +
                "   WHEN '08S30ZXi' THEN 14\n" +
                "   WHEN '08S38Y' THEN 15\n" +
                "   WHEN '08S39B' THEN 16\n" +
                "   WHEN '08S01ZW' THEN 17\n" +
                "   WHEN '08S01ZWi' THEN 18\n" +
                "   WHEN '08S40Z' THEN 19\n" +
                "   WHEN '08S01' THEN 20\n" +
                "   WHEN '08S01Z' THEN 21\n" +
                "   WHEN '08S42' THEN 22\n" +
                "   WHEN '08S31' THEN 23\n" +
                "   WHEN '08S39Y' THEN 24\n" +
                "   WHEN '08S39' THEN 25\n" +
                "     ELSE 300\n" +
                "  END\n"
        def starter = new Timestamp(searchParams.getStartDate().time)
        def endDate = new Timestamp(searchParams.getEndDate().time)
        def params = [startDate: starter, endDate: endDate, clinic: clinic.id, clinicalService: service.id]
        def sql = new Sql(dataSource as DataSource)

        def list = sql.rows(query, params)
        return list
    }

    List getQuantityRemainReportRecords(ReportSearchParams searchParams, DataSource dataSource) {
        Clinic clinic = Clinic.findById(searchParams.getClinicId())
        ClinicalService service = ClinicalService.findByCode('TARV')
        String query =
                "   SELECT  \n" +
                        "  mmiareport.packSize, \n" +
                        "  mmiareport.fnmCode, \n" +
                        "  mmiareport.drugId, \n" +
                        "  mmiareport.drugName,\n" +
                        "  mmiareport.h as received, \n" +
                        "  mmiareport.i as saidas, \n" +
                        "  ((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) as saldo, \n" +
                        "  mmiareport.total_remain \n" +
                        "  FROM \n" +
                        "   ( SELECT \n" +
                        "    dr.pack_Size as packSize, \n" +
                        "    dr.fnm_Code as fnmCode, \n" +
                        "    dr.name as drugName, \n" +
                        "    dr.id as drugId, \n" +
                        "   ( select \n" +
                        "   coalesce(sum(s.units_received),0) as r \n" +
                        "   from stock s \n" +
                        "   inner join stock_entrance se on se.id = s.entrance_id \n" +
                        "    where \n" +
                        "   se.date_received < :startDate and \n" +
                        "   s.drug_id = dr.id \n" +
                        "   and s.clinic_id = :clinic \n" +
                        "   ) as a, \n" +
                        "   ( \n" +
                        "   select \n" +
                        "   coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value \n" +
                        "   from stock_adjustment sa \n" +
                        "   inner join stock s on s.id = sa.adjusted_stock_id \n" +
                        "   inner join refered_stock_moviment rf on rf.id = sa.reference_id \n" +
                        "   inner join stock_operation_type sot on sot.id = sa.operation_id \n" +
                        "   where \n" +
                        "   sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                        "   and s.drug_id = dr.id \n" +
                        "   and (rf.date < :startDate) \n" +
                        "   and rf.clinic_id = :clinic \n" +
                        "    ) as b, \n" +
                        "   ( \n" +
                        "   select \n" +
                        "    coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value \n" +
                        "   from stock_adjustment sa \n" +
                        "   inner join stock s on s.id = sa.adjusted_stock_id \n" +
                        "   inner join inventory i on i.id = sa.inventory_id \n" +
                        "   inner join stock_operation_type sot on sot.id = sa.operation_id \n" +
                        "   where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment' \n" +
                        "   and s.drug_id = dr.id \n" +
                        "   and (i.end_date < :startDate) \n" +
                        "   and i.clinic_id = :clinic \n" +
                        "   ) as c, \n" +
                        "   ( \n" +
                        "   select \n" +
                        "   coalesce(sum(pd.quantity_supplied),0) as s \n" +
                        "   from packaged_drug pd \n" +
                        "   inner join pack pk on pk.id = pd.pack_id \n" +
                        "   where \n" +
                        "   pk.pickup_date < :startDate  and \n" +
                        "   pd.drug_id = dr.id \n" +
                        "   and pk.clinic_id = :clinic \n" +
                        "   ) as d, \n" +
                        "   ( \n" +
                        "   select \n" +
                        "   coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value \n" +
                        "   from stock_adjustment sa \n" +
                        "   inner join stock s on s.id = sa.adjusted_stock_id \n" +
                        "   inner join refered_stock_moviment rf on rf.id = sa.reference_id \n" +
                        "   inner join stock_operation_type sot on sot.id = sa.operation_id \n" +
                        "   where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                        "   and s.drug_id = dr.id \n" +
                        "   and (rf.date < :startDate) \n" +
                        "   and rf.clinic_id = :clinic \n" +
                        "   ) as e, \n" +
                        "   ( \n" +
                        "   select \n" +
                        "   coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value \n" +
                        "   from Stock_Adjustment sa \n" +
                        "   inner join stock s on s.id = sa.adjusted_stock_id \n" +
                        "   inner join inventory i on i.id = sa.inventory_id \n" +
                        "   inner join stock_operation_type sot on sot.id = sa.operation_id \n" +
                        "   where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment' \n" +
                        "   and s.drug_id = dr.id \n" +
                        "   and (i.end_date < :startDate) \n" +
                        "   and i.clinic_id = :clinic \n" +
                        "   ) as f, \n" +
                        "   (  select \n" +
                        "   coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value \n" +
                        "    from Stock_Adjustment sa \n" +
                        "    inner join stock s on s.id = sa.adjusted_stock_id \n" +
                        "    inner join destroyed_stock rf on rf.id = sa.destruction_id \n" +
                        "    inner join stock_operation_type sot on sot.id = sa.operation_id \n" +
                        "   where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                        "     and s.drug_id = dr.id \n" +
                        "     and (rf.date < :startDate) \n" +
                        "    and rf.clinic_id = :clinic \n" +
                        "     ) as g, \n" +
                        "     ( \n" +
                        "    select \n" +
                        "    coalesce(sum(s.units_received),0) as r \n" +
                        "    from stock s \n" +
                        "    inner join stock_entrance se on se.id = s.entrance_id \n" +
                        "    where \n" +
                        "    se.date_received >= :startDate \n" +
                        "    and se.date_received <= :endDate  and \n" +
                        "    s.drug_id = dr.id \n" +
                        "    and s.clinic_id = :clinic \n" +
                        "   ) as h, \n" +
                        "    ( \n" +
                        "    select \n" +
                        "   coalesce(sum(pd.quantity_supplied),0) as s \n" +
                        "   from packaged_drug pd \n" +
                        "   inner join pack pk on pk.id = pd.pack_id \n" +
                        "    where \n" +
                        "    pk.pickup_Date >= :startDate \n" +
                        "   and pk.pickup_Date <= :endDate and \n" +
                        "   pd.drug_id = dr.id \n" +
                        "   and pk.clinic_id = :clinic \n" +
                        "   ) as i, \n" +
                        "   ( \n" +
                        "           select \n" +
                        "  max(s.expire_date) \n" +
                        "    from Stock s \n" +
                        "     where s.drug_id = dr.id \n" +
                        "  and s.clinic_id = :clinic \n" +
                        "     ) as validade, \n" +
                        "   ( \n" +
                        "    select total_remain from \n" +
                        "    (WITH MaxPickupDates AS ( \n" +
                        "    SELECT pat.id AS patient_id, MAX(pack.pickup_date) AS max_pickup_date \n" +
                        "    FROM pack p \n" +
                        "    INNER JOIN packaged_drug pd ON pd.pack_id = p.id \n" +
                        "    INNER JOIN patient_visit_details pvd ON p.id = pvd.pack_id \n" +
                        "    INNER JOIN patient_visit pv ON pv.id = pvd.patient_visit_id \n" +
                        "    INNER JOIN patient pat ON pat.id = pv.patient_id \n" +
                        "    INNER JOIN drug d ON d.id = pd.drug_id \n" +
                        "    INNER JOIN pack pack ON pack.id = pvd.pack_id \n" +
                        "    GROUP BY pat.id \n" +
                        "      ) \n" +
                        "    select   q.drug_id, SUM(q.quantity_remain) as total_remain \n" +
                        "    FROM ( \n" +
                        "    select d.id as drug_id, d.name, pd.quantity_remain, pack.pickup_date, pat.id AS patient_id \n" +
                        "    FROM pack p \n" +
                        "    INNER JOIN packaged_drug pd ON pd.pack_id = p.id \n" +
                        "    INNER JOIN patient_visit_details pvd ON p.id = pvd.pack_id \n" +
                        "    INNER JOIN patient_visit pv ON pv.id = pvd.patient_visit_id \n" +
                        "    INNER JOIN patient pat ON pat.id = pv.patient_id \n" +
                        "    INNER JOIN drug d ON d.id = pd.drug_id \n" +
                        "    INNER JOIN pack pack ON pack.id = pvd.pack_id \n" +
                        "     ) q \n" +
                        "    INNER JOIN MaxPickupDates m ON q.patient_id = m.patient_id AND q.pickup_date = m.max_pickup_date \n" +
                        "    where \n" +
                        "    q.drug_id =  dr.id \n" +
                        "    group by q.drug_id ) as qtyRemain)  as total_remain \n" +
                        "    FROM drug dr \n" +
                        "    INNER JOIN Stock s ON s.drug_id = dr.id \n" +
                        "    WHERE  dr.active = true \n" +
                        "    AND s.expire_date >= :startDate \n" +
                        "     and dr.clinical_service_id = :clinicalService \n" +
                        "     ) as mmiareport \n" +
                        "    group by 1,2,3,4,5,6,7,8 \n " +
                        "   ORDER BY\n" +
                        "   CASE mmiareport.fnmCode\n" +
                        "   WHEN '08S18WI' THEN 1\n" +
                        "   WHEN '08S18W' THEN 2\n" +
                        "   WHEN '08S18WII' THEN 3\n" +
                        "   WHEN '08S18XI' THEN 4\n" +
                        "   WHEN '08S18X' THEN 5\n" +
                        "   WHEN '08S18XII' THEN 6\n" +
                        "   WHEN '08S18Z' THEN 7\n" +
                        "   WHEN '08S01ZY' THEN 8\n" +
                        "   WHEN '08S01ZZ' THEN 9\n" +
                        "   WHEN '08S30WZ' THEN 10\n" +
                        "   WHEN '08S30ZY' THEN 11\n" +
                        "   WHEN '08S39Z' THEN 12\n" +
                        "   WHEN '08S30ZX' THEN 13\n" +
                        "   WHEN '08S30ZXi' THEN 14\n" +
                        "   WHEN '08S38Y' THEN 15\n" +
                        "   WHEN '08S39B' THEN 16\n" +
                        "   WHEN '08S01ZW' THEN 17\n" +
                        "   WHEN '08S01ZWi' THEN 18\n" +
                        "   WHEN '08S40Z' THEN 19\n" +
                        "   WHEN '08S01' THEN 20\n" +
                        "   WHEN '08S01Z' THEN 21\n" +
                        "   WHEN '08S42' THEN 22\n" +
                        "   WHEN '08S31' THEN 23\n" +
                        "   WHEN '08S39Y' THEN 24\n" +
                        "   WHEN '08S39' THEN 25\n" +
                        "     ELSE 300\n" +
                        "  END\n";
        def starter = new Timestamp(searchParams.getStartDate().time)
        def endDate = new Timestamp(searchParams.getEndDate().time)
        def params = [startDate: starter, endDate: endDate, clinic: clinic.id, clinicalService: service.id]
        def sql = new Sql(dataSource as DataSource)

        def list = sql.rows(query, params)
        return list
    }


    @Override
    boolean existsBatchNumber(String batchNumber, String clinicId) {
        return !Objects.isNull( Stock.findByBatchNumberAndClinic(batchNumber, Clinic.findById(clinicId)))
    }

    @Override
    List<Stock> getAllByClinicId(String clinicId, int offset, int max) {
        return Stock.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

    @Override
    Stock getStockByBatchNumberAndClinic(String batchNumber, String clinicId) {
        return Stock.findByBatchNumberAndClinic(batchNumber, Clinic.findById(clinicId))
}
}

