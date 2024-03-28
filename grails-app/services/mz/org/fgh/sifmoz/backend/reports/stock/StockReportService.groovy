package mz.org.fgh.sifmoz.backend.reports.stock

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stock.StockService
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(StockReportTemp)
abstract class StockReportService implements IStockReportService {
    StockService stockService

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

/**
 * get report data for received stock report
 * @param reportSearchParams
 * @return
 */


    @Override
    void processReportRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        List<Stock> stocks = stockService.getReceivedStock(searchParams.getClinicId(),
                searchParams.getStartDate(),
                searchParams.getEndDate(), searchParams.getClinicalService())
        double percentageUnit = 0
        if (stocks.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else {
            percentageUnit = 100 / stocks.size()
        }

        for (Stock stock : stocks) {
            StockReportTemp reportTemp = new StockReportTemp()
            reportTemp.setYear(searchParams.getYear())
            reportTemp.setPharmacyId(searchParams.getClinicId())
            reportTemp.setProvinceId(searchParams.getProvinceId())
            reportTemp.setDistrictId(searchParams.getDistrictId())
            reportTemp.setStartDate(searchParams.getStartDate())
            reportTemp.setEndDate(searchParams.getEndDate())
            reportTemp.setPeriodType(searchParams.getPeriodType())
            reportTemp.setPeriod(searchParams.getPeriod())
            reportTemp.setReportId(searchParams.getId())
            reportTemp.setDrugName(stock.getDrug().getName())
            reportTemp.setDateReceived(stock.getEntrance().getDateReceived())
            reportTemp.setExpiryDate(stock.getExpireDate())
            reportTemp.setUnitsReceived(stock.getUnitsReceived())
            reportTemp.setOrderNumber(stock.getEntrance().getOrderNumber())
            reportTemp.setManufacture(stock.getManufacture())
            reportTemp.setBatchNumber(stock.getBatchNumber())

            processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
            if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
                setProcessMonitor(processMonitor)
            }
            reportProcessMonitorService.save(processMonitor)
            save(reportTemp)
        }

    }

    @Override
    List<StockReportTemp> getReportDataByReportId(String reportId) {
        return StockReportTemp.findAllByReportId(reportId,[sort: "dateReceived", order: "desc"])
    }

    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }


}
