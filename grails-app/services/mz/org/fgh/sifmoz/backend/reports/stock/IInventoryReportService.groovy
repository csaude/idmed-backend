package mz.org.fgh.sifmoz.backend.reports.stock

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor

interface IInventoryReportService {


    InventoryReportTemp get(Serializable id)

    List<InventoryReportTemp> list(Map args)

    Long count()

    InventoryReportTemp delete(Serializable id)

    List<InventoryReportTemp> processamentoDados (ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor)

    InventoryReportTemp save(InventoryReportTemp inventoryReportTemp)

    List<InventoryReportTemp> getReportDataByReportId(String reportId)

    void doSave(List<InventoryReportTemp> inventoryReportTemp)

    List<InventoryReportTemp> getInventoriesList(String reportId)

    List<InventoryReportResponse> getReportDataByInventoryId(String inventoryId, String reportId)

}
