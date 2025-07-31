package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IInventoryReportService {


    InventoryReport get(Serializable id)

    List<InventoryReport> list(Map args)

    Long count()

    InventoryReport delete(Serializable id)

    List<InventoryReport> processamentoDados (ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor)

    InventoryReport save(InventoryReport inventoryReportTemp)

    List<InventoryReport> getReportDataByReportId(String reportId)

    void doSave(List<InventoryReport> inventoryReportTemp)

    List<InventoryReport> getInventoriesList(String reportId)

    List<InventoryReportResponse> getReportDataByInventoryId(String inventoryId, String reportId)

}
