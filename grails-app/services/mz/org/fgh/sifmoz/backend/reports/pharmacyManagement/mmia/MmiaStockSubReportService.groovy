package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

import javax.inject.Qualifier
import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
@Service(MmiaStockSubReportItem)
abstract class MmiaStockSubReportService implements IMmiaStockSubReportService {

    @Autowired
    DataSource dataSource
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService
    @Override
    List<MmiaStockSubReportItem> generateMmiaStockSubReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        ClinicalService service = ClinicalService.findById(searchParams.getClinicalService())
        Clinic clinic = Clinic.findById(searchParams.getClinicId())

        List<MmiaStockSubReportItem> mmiaStockSubReportItems = new ArrayList<>()

        String query = "SELECT \n" +
                " mmiareport.packSize,\n" +
                " mmiareport.fnmCode,\n" +
                " mmiareport.drugName,\n" +
                " mmiareport.drugId,\n" +
                " mmiareport.h as received,\n" +
                " mmiareport.i as saidas,\n" +
                " ((mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n ) as ajustes,\n" +
                " ((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) as saldo,\n" +
                " mmiareport.validade,\n" +
                " (((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) + mmiareport.h - mmiareport.i + (mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n) as inventario\n" +
                " FROM\n" +
                " ( SELECT \n" +
                " dr.pack_Size as packSize, \n" +
                " dr.fnm_Code as fnmCode,\n" +
                " dr.name as drugName,\n" +
                " dr.id as drugId,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(s.units_received),0) as r \n" +
                " from stock s \n" +
                " inner join stock_entrance se on se.id = s.entrance_id \n" +
                " where se.date_received < :startDate\n" +
                " and s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as a,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as b,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date < :startDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as c,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_date < :startDate \n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as d,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa\n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as e,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date < :startDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as f,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as g,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(s.units_received),0) as r \n" +
                " from stock s \n" +
                " inner join stock_entrance se on se.id = s.entrance_id \n" +
                " where se.date_received >= :startDate \n" +
                " and se.date_received <= :endDate \n" +
                " and s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as h,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_Date >= :startDate \n" +
                " and pk.pickup_Date <= :endDate\n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as i,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as j,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date >= :startDate and i.end_date <= :endDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as k,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa\n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as l,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date >= :startDate and i.end_date <= :endDate)\n" +
                " and i.clinic_id = :clinic\n" +
                " ) as m,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as n,\n" +
                " (\n" +
                " select \n" +
                " max(s.expire_date) \n" +
                " from Stock s \n" +
                " where s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as validade\n" +
                "\n" +
                " FROM drug dr\n" +
                " INNER JOIN Stock s ON s.drug_id = dr.id\n" +
                " WHERE  dr.active = true AND s.expire_date >= :startDate\n" +
                " and dr.clinical_service_id = :clinicalService\n" +
                " ) as mmiareport\n" +
                " group by 1,2,3,4,5,6,7,8,9,10\n" +
                " ORDER BY\n" +
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

        def list = sql.rows(query,params)

        double percUnit = 0.00
        if (Utilities.listHasElements(list as ArrayList<?>)) {
            percUnit = 35 / list.size()
            for (int i = 0; i < list.size(); i++) {

                generateAndSaveMmiaStockSubReport(list[i], mmiaStockSubReportItems, searchParams.getId())
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                reportProcessMonitorService.save(processMonitor)
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

            return mmiaStockSubReportItems
        }
        else {
            percUnit = 35
            return null
        }
    }

    @Override
    List<MmiaStockSubReportItem> generateMmiaStockSubReportForMMiaTB(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        ClinicalService service = ClinicalService.findById(searchParams.getClinicalService())
//        ClinicalService serviceTB = ClinicalService.findByCode('TB')
        Clinic clinic = Clinic.findById(searchParams.getClinicId())

        List<MmiaStockSubReportItem> mmiaStockSubReportItems = new ArrayList<>()

        String query = "SELECT \n" +
                " mmiareport.packSize,\n" +
                " mmiareport.fnmCode,\n" +
                " mmiareport.drugName,\n" +
                " mmiareport.drugId,\n" +
                " mmiareport.h as received,\n" +
                " mmiareport.i as saidas,\n" +
                " ((mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n ) as ajustes,\n" +
                " ((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) as saldo,\n" +
                " mmiareport.validade,\n" +
                " (((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.c + mmiareport.f) - mmiareport.g) + mmiareport.h - mmiareport.i + (mmiareport.j + mmiareport.l) - (mmiareport.k + mmiareport.m) - mmiareport.n) as inventario\n" +
                " FROM\n" +
                " ( SELECT \n" +
                " dr.pack_Size as packSize, \n" +
                " dr.fnm_Code as fnmCode,\n" +
                " dr.name as drugName,\n" +
                " dr.id as drugId,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(s.units_received),0) as r \n" +
                " from stock s \n" +
                " inner join stock_entrance se on se.id = s.entrance_id \n" +
                " where se.date_received < :startDate\n" +
                " and s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as a,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as b,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date < :startDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as c,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_date < :startDate \n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as d,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa\n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as e,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStock_Adjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date < :startDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as f,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date < :startDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as g,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(s.units_received),0) as r \n" +
                " from stock s \n" +
                " inner join stock_entrance se on se.id = s.entrance_id \n" +
                " where se.date_received >= :startDate \n" +
                " and se.date_received <= :endDate \n" +
                " and s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as h,\n" +
                " (\n" +
                " select \n" +
                " coalesce(sum(pd.quantity_supplied),0) as s \n" +
                " from packaged_drug pd \n" +
                " inner join pack pk on pk.id = pd.pack_id \n" +
                " where pk.pickup_Date >= :startDate \n" +
                " and pk.pickup_Date <= :endDate\n" +
                " and pd.drug_id = dr.id \n" +
                " and pk.clinic_id = :clinic\n" +
                " ) as i,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate) \n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as j,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from stock_adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date >= :startDate and i.end_date <= :endDate) \n" +
                " and i.clinic_id = :clinic\n" +
                " ) as k,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE (-1*sa.adjusted_value) END),0) as adjusted_value\n" +
                " from stock_adjustment sa\n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join refered_stock_moviment rf on rf.id = sa.reference_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as l,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id\n" +
                " inner join inventory i on i.id = sa.inventory_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'\n" +
                " and s.drug_id = dr.id \n" +
                " and (i.end_date >= :startDate and i.end_date <= :endDate)\n" +
                " and i.clinic_id = :clinic\n" +
                " ) as m,\n" +
                " (\n" +
                " select \n" +
                " coalesce(SUM(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE (-1*sa.adjusted_Value) END),0) as adjusted_Value\n" +
                " from Stock_Adjustment sa \n" +
                " inner join stock s on s.id = sa.adjusted_stock_id  \n" +
                " inner join destroyed_stock rf on rf.id = sa.destruction_id\n" +
                " inner join stock_operation_type sot on sot.id = sa.operation_id\n" +
                " where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' \n" +
                " and s.drug_id = dr.id \n" +
                " and (rf.date >= :startDate and rf.date <= :endDate)\n" +
                " and rf.clinic_id = :clinic\n" +
                " ) as n,\n" +
                " (\n" +
                " select \n" +
                " max(s.expire_date) \n" +
                " from Stock s \n" +
                " where s.drug_id = dr.id \n" +
                " and s.clinic_id = :clinic\n" +
                " ) as validade\n" +
                "\n" +
                " FROM drug dr\n" +
                " INNER JOIN Stock s ON s.drug_id = dr.id\n" +
                " WHERE  dr.active = true AND s.expire_date >= :startDate\n" +
                " and dr.clinical_service_id = :clinicalService\n" +
                " ) as mmiareport\n" +
                " group by 1,2,3,4,5,6,7,8,9,10\n" +
                " ORDER BY\n" +
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

        def list = sql.rows(query,params)

        double percUnit = 0.00
        if (Utilities.listHasElements(list as ArrayList<?>)) {
            percUnit = 35 / list.size()
            for (int i = 0; i < list.size(); i++) {

                generateAndSaveMmiaStockSubReport(list[i], mmiaStockSubReportItems, searchParams.getId())
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                reportProcessMonitorService.save(processMonitor)
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

            return mmiaStockSubReportItems
        }
        else {
            percUnit = 35
            return null
        }
    }

    void generateAndSaveMmiaStockSubReport(Object item, List<MmiaStockSubReportItem> mmiaStockSubReportItems, String reportId) {
        MmiaStockSubReportItem stockSubReportItem = new MmiaStockSubReportItem()
        stockSubReportItem.setReportId(reportId)
        stockSubReportItem.setUnit(String.valueOf(item[0]))
        stockSubReportItem.setFnmCode(String.valueOf(item[1]))
        stockSubReportItem.setDrugName(String.valueOf(item[2]))
        stockSubReportItem.setInitialEntrance(Integer.valueOf(String.valueOf(item[4])))
        stockSubReportItem.setOutcomes(String.valueOf(item[5].toString()).toDouble().intValue())
        stockSubReportItem.setLossesAdjustments(Integer.valueOf(String.valueOf(item[6])))
        stockSubReportItem.setInventory(String.valueOf(item[9].toString()).toDouble().intValue())
        stockSubReportItem.setExpireDate(item[8] as Date)
        stockSubReportItem.setBalance(String.valueOf(item[7].toString()).toDouble().intValue())
        save(stockSubReportItem)
        mmiaStockSubReportItems.add(stockSubReportItem)
    }
}
