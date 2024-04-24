package mz.org.fgh.sifmoz.backend.stockinventory


import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.stock.InventoryReportTemp

interface IInventoryService {

    Inventory get(Serializable id)

    List<Inventory> list(Map args)

    Long count()

    Inventory delete(Serializable id)

    Inventory save(Inventory inventory)

    void processInventoryAdjustments(Inventory inventory)

    void initInventory(Inventory inventory)

    List<Inventory> getAllByClinicId(String clinicId, int offset, int max)

    Inventory removeInventory(String id)

    boolean isInventoryPeriod(String clinicId)

    List getPartialInventories(ReportSearchParams reportSearchParams)

    List<InventoryReportTemp> getInventoryListByReportId(String reportId)

}