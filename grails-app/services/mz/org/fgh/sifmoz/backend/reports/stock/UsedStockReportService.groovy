package mz.org.fgh.sifmoz.backend.reports.stock

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.stock.StockService
import org.apache.commons.lang.StringUtils
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource
import java.sql.Timestamp

@Transactional
@Service(UsedStockReportTemp)
abstract class UsedStockReportService implements IUsedStockReportService {

    StockService stockService
    @Autowired
    DataSource dataSource
    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    double percentageUnit


    public static final String PROCESS_STATUS_PROCESSING_FINISHED = "Processamento terminado"

/**
 * get report data for received stock report
 * @param reportSearchParams
 * @return
 */
    @Override
    List<UsedStockReportTemp> getReportDataByReportId(String reportId) {
        return UsedStockReportTemp.findAllByReportId(reportId)
    }

    void processReportUsedStockRecords(ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        List list
        if (searchParams.getReportType().equalsIgnoreCase("USED_STOCK")) {
            list = stockService.getUsedStockReportRecords(searchParams, dataSource)
        } else {
            list = stockService.getQuantityRemainReportRecords(searchParams, dataSource)
        }

        if (list.size() == 0) {
            setProcessMonitor(processMonitor)
            reportProcessMonitorService.save(processMonitor)
        } else { 
            percentageUnit = 100 / list.size()
 
        }


        if (searchParams.getReportType().equalsIgnoreCase("USED_STOCK")) {
            for (int i = 0; i < list.size(); i++) {
                generateAndSaveUsedStockSubReport(list[i], searchParams, processMonitor)
            }
        } else {
            for (int i = 0; i < list.size(); i++) {
                generateAndSaveQuantityRemainReport(list[i], searchParams, processMonitor)
            }
        }
    }

    void generateAndSaveUsedStockSubReport(Object item, ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        UsedStockReportTemp reportTemp = new UsedStockReportTemp()
        reportTemp.setReportId(searchParams.getId())
        reportTemp.setYear(searchParams.getYear())
        reportTemp.setPharmacyId(searchParams.getClinicId())
        reportTemp.setProvinceId(searchParams.getProvinceId())
        reportTemp.setDistrictId(searchParams.getDistrictId())
        reportTemp.setStartDate(searchParams.getStartDate())
        reportTemp.setEndDate(searchParams.getEndDate())
        reportTemp.setPeriodType(searchParams.getPeriodType())
        reportTemp.setPeriod(searchParams.getPeriod())
        reportTemp.setReportId(searchParams.getId())
        reportTemp.setFnName(String.valueOf(item[1]))
        reportTemp.setDrugName(String.valueOf(item[2]))
        reportTemp.setDrugId(item[3] as String)
        reportTemp.setReceivedStock((long) Double.parseDouble(String.valueOf(item[4]))) //initial entrance
        reportTemp.setStockIssued((long) Double.parseDouble(String.valueOf(item[5]))) //outcomes
        reportTemp.setAdjustment((long) Double.parseDouble(String.valueOf(item[6])))
        reportTemp.setActualStock((long) Double.parseDouble(String.valueOf(item[9]))) //inventario
        reportTemp.setDestroyedStock((long) Double.parseDouble(0.0 as String)) //ver
        reportTemp.setBalance((long) Double.parseDouble(String.valueOf(item[7])))
        reportTemp.setQuantityRemain(StringUtils.EMPTY)
        reportTemp.setNotes(StringUtils.EMPTY)
        reportTemp.setPackSize(StringUtils.EMPTY)

        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)
        save(reportTemp)
    }


    void generateAndSaveQuantityRemainReport(Object item, ReportSearchParams searchParams, ReportProcessMonitor processMonitor) {
        UsedStockReportTemp reportTemp = new UsedStockReportTemp()
        reportTemp.setReportId(searchParams.getId())
        reportTemp.setYear(searchParams.getYear())
        reportTemp.setPharmacyId(searchParams.getClinicId())
        reportTemp.setProvinceId(searchParams.getProvinceId())
        reportTemp.setDistrictId(searchParams.getDistrictId())
        reportTemp.setStartDate(searchParams.getStartDate())
        reportTemp.setEndDate(searchParams.getEndDate())
        reportTemp.setPeriodType(searchParams.getPeriodType())
        reportTemp.setPeriod(searchParams.getPeriod())
        reportTemp.setReportId(searchParams.getId())
        reportTemp.setPackSize(String.valueOf(item[0]))
        reportTemp.setFnName(String.valueOf(item[1]))
        reportTemp.setDrugId(item[2] as String)
        reportTemp.setDrugName(String.valueOf(item[3]))
        reportTemp.setReceivedStock((long) Double.parseDouble(String.valueOf(item[4]))) //initial entrance
        reportTemp.setStockIssued((long) Double.parseDouble(String.valueOf(item[5]))) //outcomes
        reportTemp.setBalance((long) Double.parseDouble(String.valueOf(item[6])))
        reportTemp.setDestroyedStock(0) //ver
        reportTemp.setAdjustment(0)
        reportTemp.setActualStock(0)

        long totalQuantityRemain = item[7]!=null?(long) Double.parseDouble(String.valueOf(item[7])):0;
        long packSize =  Long.parseLong(String.valueOf(item[0]))

        String notes = totalQuantityRemain == 0? "-" : Math.floor(totalQuantityRemain/packSize).intValue() +"frasco (s) e "+ totalQuantityRemain%packSize +" unidades de sobra";
        reportTemp.setQuantityRemain(Math.floor(totalQuantityRemain/packSize).intValue() +"("+ (totalQuantityRemain%packSize)  +")")
        reportTemp.setNotes(notes)

        processMonitor.setProgress(processMonitor.getProgress() + percentageUnit)
        if (100 == processMonitor.progress.intValue() || 99 == processMonitor.progress.intValue()) {
            setProcessMonitor(processMonitor)
        }
        reportProcessMonitorService.save(processMonitor)
        save(reportTemp)
    }


    private ReportProcessMonitor setProcessMonitor(ReportProcessMonitor processMonitor) {
        processMonitor.setProgress(100)
        processMonitor.setMsg(PROCESS_STATUS_PROCESSING_FINISHED)
    }


}
