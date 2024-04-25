package mz.org.fgh.sifmoz.backend.reports.stock

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.IReportProcessMonitorService
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.stockinventory.IInventoryService
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.SimpleDateFormat
import java.util.stream.Collectors

@Transactional
@Service(InventoryReportTemp)
abstract class InventoryReportService implements IInventoryReportService {

    @Autowired
    IReportProcessMonitorService reportProcessMonitorService

    @Autowired
    IInventoryService inventoryService

    @Autowired
    SessionFactory sessionFactory

    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd")

    @Override
    InventoryReportTemp get(Serializable id) {
        return InventoryReportTemp.findById(id as String)
    }

    @Override
    List<InventoryReportTemp> list(Map args) {
        return null
    }

    @Override
    Long count() {
        return null
    }

    @Override
    InventoryReportTemp delete(Serializable id) {
        return null
    }

    @Override
    void doSave(List<InventoryReportTemp> inventories) {
        for (inventoryReportTemp in inventories) {
            save(inventoryReportTemp)
        }
    }

    @Override
    List<InventoryReportTemp> getReportDataByReportId(String reportId) {
        def res = InventoryReportTemp.findAllByReportId(reportId)
        return res
    }

    @Override
    List<InventoryReportResponse> getReportDataByInventoryId(String inventoryId, String reportId) {
        def res = InventoryReportTemp.findAllByInventoryIdAndReportId(inventoryId, reportId)

        Map<String, List<InventoryReportTemp>> result = res.stream()
                .collect(Collectors.groupingBy(InventoryReportTemp::getDrugName));
        List <InventoryReportResponse> response = new ArrayList<>()

        for (Map.Entry<String, List<InventoryReportTemp>> entry : result.entrySet()) {
            String drugName = entry.getKey();
            List<InventoryReportTemp> adjustments = entry.getValue();

            Long totalAdjustments = adjustments.stream()
                    .mapToLong(InventoryReportTemp::getAdjustedValue)
                    .sum();

            Long totalBalance = adjustments.stream()
                    .mapToLong(InventoryReportTemp::getBalance)
                    .sum();

            InventoryReportResponse item = new InventoryReportResponse()
            item.setDrugName(drugName)
            item.setTotalAdjustedValue(totalAdjustments)
            item.setTotalBalance(totalBalance)
            item.setAdjustments(adjustments)
            item.setId(UUID.randomUUID().toString())
            response.add(item)
        }
        return response
    }

    @Override
    List<InventoryReportTemp> processamentoDados(ReportSearchParams reportSearchParams, ReportProcessMonitor processMonitor) {

        List<InventoryReportTemp> resultList = new ArrayList<>()
        String reportId = reportSearchParams.getId()
        Clinic clinic = Clinic.findById(reportSearchParams.clinicId)

        def result

        result = inventoryService.getInventoriesData(reportSearchParams)

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            double percUnit = 100.0 / result.size()

            for (item in result) {
                InventoryReportTemp inventoryReportTemp = setGenericInfo(reportSearchParams, clinic)
                processMonitor.setProgress(processMonitor.getProgress() + percUnit)
                generateAndSaveInventories(item as List, inventoryReportTemp, reportId, reportSearchParams)
                resultList.add(inventoryReportTemp)
                if (processMonitor.getProgress() > 99.6) processMonitor.setProgress(100)
                ReportProcessMonitor.withNewTransaction {
                    processMonitor.save(flush: true)
                }
            }

            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)

            return resultList
        } else {
            processMonitor.setProgress(100)
            processMonitor.setMsg("Processamento terminado")
            reportProcessMonitorService.save(processMonitor)
            return new ArrayList<InventoryReportTemp>()
        }
    }

    @Override
    List<Inventory> getInventoriesList(String reportId) {
        def result = inventoryService.getInventoryListByReportId(reportId)
        List<Inventory> resultList = new ArrayList<>()
        for (item in result) {
            Inventory inventory = new Inventory()
            inventory.setId(item[0])
            inventory.setEndDate(item[1])
            resultList.add(inventory)
        }
        return resultList
    }


    private InventoryReportTemp setGenericInfo(ReportSearchParams searchParams, Clinic clinic) {
        InventoryReportTemp inventoryReportTemp = new InventoryReportTemp()
        inventoryReportTemp.setClinic(clinic.getClinicName())
        inventoryReportTemp.setStartDate(searchParams.startDate)
        inventoryReportTemp.setEndDate(searchParams.endDate)
        inventoryReportTemp.setReportId(searchParams.id)
        inventoryReportTemp.setPeriodType(searchParams.periodType)
        inventoryReportTemp.setReportId(searchParams.id)
        inventoryReportTemp.setYear(searchParams.year)
        inventoryReportTemp.setProvince(Province.findById(clinic.province.id).description)
        clinic.district == null? inventoryReportTemp.setDistrict("") : inventoryReportTemp.setDistrict(District.findById(clinic.district.id).description)

        return inventoryReportTemp
    }

    void generateAndSaveInventories(List item, InventoryReportTemp inventoryReportTemp, String reportId, ReportSearchParams searchParams) {

        inventoryReportTemp.setReportId(reportId)
        Date startDate =formatter.parse(item[0].toString())
        inventoryReportTemp.setInventoryStartDate(startDate)
        Date endDate =formatter.parse(item[1].toString())
        inventoryReportTemp.setInventoryEndDate(endDate)
        Date captureDate =formatter.parse(item[1].toString())
        inventoryReportTemp.setCaptureDate(captureDate)
        inventoryReportTemp.setDrugName(item[3])
        inventoryReportTemp.setBatchNumber(item[4])
        Date expireDate =formatter.parse(item[1].toString())
        inventoryReportTemp.setExpireDate(expireDate)
        inventoryReportTemp.setBalance((long) Double.parseDouble(String.valueOf(item[6])) )
        inventoryReportTemp.setAdjustedValue((long) Double.parseDouble(String.valueOf(item[7])))
        inventoryReportTemp.setFormDescription(item[8])
        inventoryReportTemp.setNotes(item[9])
        inventoryReportTemp.setInventoryId(item[10])

        try {
            save(inventoryReportTemp)
        } catch (Exception e) {
            println(e.message)
        }
    }

}
