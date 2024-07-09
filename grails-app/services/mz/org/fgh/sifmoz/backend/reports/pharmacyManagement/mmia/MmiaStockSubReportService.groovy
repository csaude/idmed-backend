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

        String query =
        """
        SELECT 
         mmiareport.packSize,
         mmiareport.fnmCode,
         mmiareport.drugName,
         mmiareport.drugId,
         mmiareport.h as received,
         mmiareport.i as saidas,
         ((mmiareport.j + mmiareport.k) - (mmiareport.l + mmiareport.m) - mmiareport.n ) as ajustes,
         ((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.e + mmiareport.f + mmiareport.g)) as saldo,
         mmiareport.validade,
         ((((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.e + mmiareport.f + mmiareport.g)) 
           + mmiareport.h - mmiareport.i + (mmiareport.j + mmiareport.k) - (mmiareport.l + mmiareport.m) - mmiareport.n )) as inventario
        FROM
         ( SELECT 
             dr.pack_Size as packSize, 
             dr.fnm_Code as fnmCode,
             dr.name as drugName,
             dr.id as drugId,
             (
             select 
             COALESCE(sum(ceil(s.units_received)),0)::integer as r 
             from stock s 
             inner join stock_entrance se on se.id = s.entrance_id 
             where se.date_received < :startDate
             and s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as a,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as b,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and (i.end_date < :startDate) 
             and i.clinic_id = :clinic
             ) as c,
             (
             select 
             COALESCE(sum(ceil(pd.quantity_supplied)),0)::integer as s 
             from packaged_drug pd 
             inner JOIN packaged_drug_stock pds ON pd.id::text = pds.packaged_drug_id::text
             inner join pack pk on pk.id = pd.pack_id 
             inner JOIN stock s ON s.id::text = pds.stock_id::text
             where pk.pickup_date < :startDate 
             and pd.drug_id = dr.id 
             and pk.clinic_id = :clinic
             ) as d,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as e,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and (i.end_date < :startDate) 
             and i.clinic_id = :clinic
             ) as f,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join destroyed_stock rf on rf.id = sa.destruction_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as g,
             (
             select 
             COALESCE(sum(ceil(s.units_received)),0)::integer as r 
             from stock s 
             inner join stock_entrance se on se.id = s.entrance_id 
             where se.date_received >= :startDate 
             and se.date_received <= :endDate 
             and s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as h,
             (
             select 
             COALESCE(sum(ceil(pd.quantity_supplied)),0) as s 
             from packaged_drug pd 
             inner JOIN packaged_drug_stock pds ON pd.id::text = pds.packaged_drug_id::text
             inner join pack pk on pk.id = pd.pack_id 
             inner JOIN stock s ON s.id::text = pds.stock_id::text
             where pk.pickup_Date >= :startDate 
             and pk.pickup_Date <= :endDate
             and pd.drug_id = dr.id 
             and pk.clinic_id = :clinic
             ) as i,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date >= :startDate and rf.date <= :endDate)
             and s.expire_date >= :startDate
             and rf.clinic_id = :clinic
             ) as j,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (i.end_date >= :startDate and i.end_date <= :endDate) 
             and i.clinic_id = :clinic
             ) as k,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id
             and s.expire_date >= :startDate
             and (rf.date >= :startDate and rf.date <= :endDate)
             and rf.clinic_id = :clinic
             ) as l,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (i.end_date >= :startDate and i.end_date <= :endDate)
             and i.clinic_id = :clinic
             ) as m,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join destroyed_stock rf on rf.id = sa.destruction_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' 
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (rf.date >= :startDate and rf.date <= :endDate)
             and rf.clinic_id = :clinic
             ) as n,
             (
             select 
             max(s.expire_date) 
             from Stock s 
             where s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as validade
         FROM drug dr
         INNER JOIN Stock s ON s.drug_id = dr.id
         WHERE  dr.active = true AND s.expire_date >= :startDate
         and dr.clinical_service_id = :clinicalService
         ) as mmiareport
    GROUP BY 1,2,3,4,5,6,7,8,9,10
    ORDER BY
       CASE mmiareport.fnmCode
       WHEN '08S18WI' THEN 1
       WHEN '08S18W' THEN 2
       WHEN '08S18WII' THEN 3
       WHEN '08S18XI' THEN 4
       WHEN '08S18X' THEN 5
       WHEN '08S18XII' THEN 6
       WHEN '08S18Z' THEN 7
       WHEN '08S01ZY' THEN 8
       WHEN '08S01ZZ' THEN 9
       WHEN '08S30WZ' THEN 10
       WHEN '08S30ZY' THEN 11
       WHEN '08S39Z' THEN 12
       WHEN '08S30ZX' THEN 13
       WHEN '08S30ZXi' THEN 14
       WHEN '08S38Y' THEN 15
       WHEN '08S39B' THEN 16
       WHEN '08S01ZW' THEN 17
       WHEN '08S01ZWi' THEN 18
       WHEN '08S40Z' THEN 19
       WHEN '08S01' THEN 20
       WHEN '08S01Z' THEN 21
       WHEN '08S42' THEN 22
       WHEN '08S31' THEN 23
       WHEN '08S39Y' THEN 24
       WHEN '08S39' THEN 25
         ELSE 300
      END
    """
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

        String query =
                """
        SELECT 
         mmiareport.packSize,
         mmiareport.fnmCode,
         mmiareport.drugName,
         mmiareport.drugId,
         mmiareport.h as received,
         mmiareport.i as saidas,
         ((mmiareport.j + mmiareport.k) - (mmiareport.l + mmiareport.m) - mmiareport.n ) as ajustes,
         ((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.e + mmiareport.f + mmiareport.g)) as saldo,
         mmiareport.validade,
         ((((mmiareport.a + mmiareport.b + mmiareport.c) - (mmiareport.d + mmiareport.e + mmiareport.f + mmiareport.g)) 
           + mmiareport.h - mmiareport.i + (mmiareport.j + mmiareport.k) - (mmiareport.l + mmiareport.m) - mmiareport.n )) as inventario
        FROM
         ( SELECT 
             dr.pack_Size as packSize, 
             dr.fnm_Code as fnmCode,
             dr.name as drugName,
             dr.id as drugId,
             (
             select 
             COALESCE(sum(ceil(s.units_received)),0)::integer as r 
             from stock s 
             inner join stock_entrance se on se.id = s.entrance_id 
             where se.date_received < :startDate
             and s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as a,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as b,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and (i.end_date < :startDate) 
             and i.clinic_id = :clinic
             ) as c,
             (
             select 
             COALESCE(sum(ceil(pd.quantity_supplied)),0)::integer as s 
             from packaged_drug pd 
             inner JOIN packaged_drug_stock pds ON pd.id::text = pds.packaged_drug_id::text
             inner join pack pk on pk.id = pd.pack_id 
             inner JOIN stock s ON s.id::text = pds.stock_id::text
             where pk.pickup_date < :startDate 
             and pd.drug_id = dr.id 
             and pk.clinic_id = :clinic
             ) as d,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as e,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and (i.end_date < :startDate) 
             and i.clinic_id = :clinic
             ) as f,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join destroyed_stock rf on rf.id = sa.destruction_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date < :startDate) 
             and rf.clinic_id = :clinic
             ) as g,
             (
             select 
             COALESCE(sum(ceil(s.units_received)),0)::integer as r 
             from stock s 
             inner join stock_entrance se on se.id = s.entrance_id 
             where se.date_received >= :startDate 
             and se.date_received <= :endDate 
             and s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as h,
             (
             select 
             COALESCE(sum(ceil(pd.quantity_supplied)),0) as s 
             from packaged_drug pd 
             inner JOIN packaged_drug_stock pds ON pd.id::text = pds.packaged_drug_id::text
             inner join pack pk on pk.id = pd.pack_id 
             inner JOIN stock s ON s.id::text = pds.stock_id::text
             where pk.pickup_Date >= :startDate 
             and pk.pickup_Date <= :endDate
             and pd.drug_id = dr.id 
             and pk.clinic_id = :clinic
             ) as i,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id 
             and (rf.date >= :startDate and rf.date <= :endDate)
             and s.expire_date >= :startDate
             and rf.clinic_id = :clinic
             ) as j,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_POSETIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from stock_adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (i.end_date >= :startDate and i.end_date <= :endDate) 
             and i.clinic_id = :clinic
             ) as k,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_value ELSE 0 END)),0)::integer as adjusted_value
             from stock_adjustment sa
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join refered_stock_moviment rf on rf.id = sa.reference_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockReferenceAdjustment' 
             and s.drug_id = dr.id
             and s.expire_date >= :startDate
             and (rf.date >= :startDate and rf.date <= :endDate)
             and rf.clinic_id = :clinic
             ) as l,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id
             inner join inventory i on i.id = sa.inventory_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment'
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (i.end_date >= :startDate and i.end_date <= :endDate)
             and i.clinic_id = :clinic
             ) as m,
             (
             select 
             COALESCE(sum(ceil(CASE sot.code WHEN 'AJUSTE_NEGATIVO' THEN sa.adjusted_Value ELSE 0 END)),0)::integer as adjusted_Value
             from Stock_Adjustment sa 
             inner join stock s on s.id = sa.adjusted_stock_id  
             inner join destroyed_stock rf on rf.id = sa.destruction_id
             inner join stock_operation_type sot on sot.id = sa.operation_id
             where sa.class = 'mz.org.fgh.sifmoz.backend.stockadjustment.StockDestructionAdjustment' 
             and s.drug_id = dr.id 
             and s.expire_date >= :startDate
             and (rf.date >= :startDate and rf.date <= :endDate)
             and rf.clinic_id = :clinic
             ) as n,
             (
             select 
             max(s.expire_date) 
             from Stock s 
             where s.drug_id = dr.id 
             and s.clinic_id = :clinic
             ) as validade
         FROM drug dr
         INNER JOIN Stock s ON s.drug_id = dr.id
         WHERE  dr.active = true AND s.expire_date >= :startDate
         and dr.clinical_service_id = :clinicalService
         ) as mmiareport
    GROUP BY 1,2,3,4,5,6,7,8,9,10
    ORDER BY
       CASE mmiareport.fnmCode
       WHEN '08S18WI' THEN 1
       WHEN '08S18W' THEN 2
       WHEN '08S18WII' THEN 3
       WHEN '08S18XI' THEN 4
       WHEN '08S18X' THEN 5
       WHEN '08S18XII' THEN 6
       WHEN '08S18Z' THEN 7
       WHEN '08S01ZY' THEN 8
       WHEN '08S01ZZ' THEN 9
       WHEN '08S30WZ' THEN 10
       WHEN '08S30ZY' THEN 11
       WHEN '08S39Z' THEN 12
       WHEN '08S30ZX' THEN 13
       WHEN '08S30ZXi' THEN 14
       WHEN '08S38Y' THEN 15
       WHEN '08S39B' THEN 16
       WHEN '08S01ZW' THEN 17
       WHEN '08S01ZWi' THEN 18
       WHEN '08S40Z' THEN 19
       WHEN '08S01' THEN 20
       WHEN '08S01Z' THEN 21
       WHEN '08S42' THEN 22
       WHEN '08S31' THEN 23
       WHEN '08S39Y' THEN 24
       WHEN '08S39' THEN 25
         ELSE 300
      END
    """
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
