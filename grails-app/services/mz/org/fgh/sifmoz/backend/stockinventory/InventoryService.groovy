package mz.org.fgh.sifmoz.backend.stockinventory

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.stock.InventoryReportTemp
import mz.org.fgh.sifmoz.backend.stock.IStockService
import mz.org.fgh.sifmoz.backend.stock.Stock

import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustmentService
import mz.org.fgh.sifmoz.backend.stockadjustment.StockAdjustment
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(Inventory)
abstract class InventoryService implements IInventoryService{
    IStockService stockService

    InventoryStockAdjustmentService adjustmentService

    @Autowired
    SessionFactory sessionFactory

    @Override
    void processInventoryAdjustments(Inventory inventory) {

        for (StockAdjustment adjustment : inventory.getAdjustments()) {
            adjustment.setFinalised(true)
        }
    }

    @Override
    void initInventory(Inventory inventory) {
        List<Stock> drugStocks = new ArrayList<>()

        for (Drug drug : inventory.getInventoryDrugs()) {
            drugStocks.addAll(stockService.findAllOnceReceivedByDrug(drug))
        }

        initInventoryAdjustments(inventory, drugStocks)

    }

    private static void initInventoryAdjustments(Inventory inventory, List<Stock> stocks) {
        List<InventoryStockAdjustment> adjustments = new ArrayList<>()

        for(Stock stock : stocks){
            adjustments.add(initAdjustment(inventory, stock))
        }
        inventory.setAdjustments(adjustments as Set<InventoryStockAdjustment>)
    }

    private static InventoryStockAdjustment initAdjustment(Inventory inventory, Stock stock) {
        StockAdjustment adjustment = new InventoryStockAdjustment(inventory, stock)
        return adjustment
    }

    @Override
    List<Inventory> getAllByClinicId(String clinicId, int offset, int max) {
        return Inventory.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
    }
    @Override
    Inventory removeInventory(String id) {
        for (InventoryStockAdjustment adj :  InventoryStockAdjustment.findAllByInventory(Inventory.findById(id))) {
            adjustmentService.delete(adj.id)
        }
       return delete(id)

    }
    @Override
    boolean isInventoryPeriod(String clinicId) {
        List<Inventory> list = Inventory.executeQuery(" select i from Inventory i "+
                "where  i.clinic.id =: clinicId and i.open = false order by i.endDate desc " ,
                [clinicId: clinicId]);
        if (list.size() > 0) {
            Inventory inventory = list.first()
            Date otherDate = inventory.endDate
            long differenceInMilliseconds = new Date().time - otherDate.time
            long differenceInDays = differenceInMilliseconds / (24 * 60 * 60 * 1000)
            return differenceInDays > 28
        } else {
            return true
        }

    }


    @Override
    List getPartialInventories(ReportSearchParams reportSearchParams) {
        def queryString = " select sa.capture_date, " +
                "i.start_date, " +
                "i.end_date," +
                "d.name, " +
                "s.batch_number, " +
                "s.expire_date, " +
                "sa.adjusted_value, " +
                "s.stock_moviment, " +
                "f.description as formDescription, " +
                "sa.notes,"+
                "i.id as inventoryId " +
                "from stock_adjustment sa \n" +
                "inner join inventory i on (i.id = sa.inventory_id)\n" +
                "inner join stock s on s.id = sa.adjusted_stock_id \n" +
                "inner join drug d on d.id = s.drug_id \n" +
                "inner join form f on f.id = d.form_id \n" +
                "where i.generic=false and i.clinic_id =:clinicId  and i.end_date >=:startDate    and i.end_date <=:endDate"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("endDate", reportSearchParams.endDate)
        query.setParameter("startDate", reportSearchParams.startDate)
        query.setParameter("clinicId", reportSearchParams.clinicId)
        List<Object[]> list = query.list()
        return list
    }

    @Override
    List<InventoryReportTemp> getInventoryListByReportId(String reportId) {
        def queryString = " select distinct i.inventory_id, i.inventory_end_date " +
                            "from inventory_report_temp i " +
                            "where i.report_id=:reportId "

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("reportId", reportId)

        List<Object[]> list = query.list()
        return list
    }


}
