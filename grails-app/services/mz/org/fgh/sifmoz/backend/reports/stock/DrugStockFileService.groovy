package mz.org.fgh.sifmoz.backend.reports.stock

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinic.ClinicService
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.drug.DrugService
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia.MmiaStockSubReportItem
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import mz.org.fgh.sifmoz.dashboard.DashboardServiceButton
import mz.org.fgh.sifmoz.stock.DrugFile
import mz.org.fgh.sifmoz.stock.DrugStockFileEvent
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.text.DateFormatSymbols

@Transactional
class DrugStockFileService {

    @Autowired
    SessionFactory sessionFactory

    def serviceMethod() {

    }

    def List<DrugStockFileEvent> getDrugSumaryEvents(String clinicId, String drugId) {
        List<DrugStockFileEvent> drugStockFileEventArrayList = new ArrayList<>()
        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from drug_stock_summary_vw  " +
                "where drug_id = :drug " +
                "   and clinic_id = :clinic "+
                " order by event_year asc, event_month asc"




        def query = session.createSQLQuery(queryString)
        query.setParameter("drug", drugId)
        query.setParameter("clinic", clinicId)
        List<Object[]> result = query.list()
        if (Utilities.listHasElements(result as ArrayList<?>)) {
            initStockEventSummary(result, drugStockFileEventArrayList)
        }

        return drugStockFileEventArrayList.reverse()
    }


    def List<DrugStockFileEvent> getDrugSumaryEventsMobile(String clinicId, String drugId) {
        List<DrugStockFileEvent> drugStockFileEventArrayList = new ArrayList<>()
        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from drug_stock_summary_mobile_vw  " +
                "where drug_id = :drug " +
                "   and clinic_id = :clinic "


        def query = session.createSQLQuery(queryString)
        query.setParameter("drug", drugId)
        query.setParameter("clinic", clinicId)
        List<Object[]> result = query.list()

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            initStockEventMobile(result, drugStockFileEventArrayList)
        }

        return drugStockFileEventArrayList.reverse()
    }

    def List<DrugStockFileEvent> getDrugBatchSumaryEvents(String clinicId, String stockId) {
        List<DrugStockFileEvent> drugStockFileEventArrayList = new ArrayList<>()

        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from drug_stock_batch_summary_vw  " +
                "where stock = :stock " +
                "   and clinic_id = :clinic " +
                "order by event_date asc"


        def query = session.createSQLQuery(queryString)
        query.setParameter("stock", stockId)
        query.setParameter("clinic", clinicId)
        List<Object[]> result = query.list()

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            initStockEventBatch(result, drugStockFileEventArrayList)
        }

        return drugStockFileEventArrayList.reverse()
    }


    def List<DrugStockFileEvent> getDrugBatchSumaryEventsMobile(String clinicId, String stockId) {
        List<DrugStockFileEvent> drugStockFileEventArrayList = new ArrayList<>()

        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from drug_stock_batch_summary_mobile_vw  " +
                "where stock = :stock " +
                "   and clinic_id = :clinic " +
                "order by event_date desc"


        def query = session.createSQLQuery(queryString)
        query.setParameter("stock", stockId)
        query.setParameter("clinic", clinicId)
        List<Object[]> result = query.list()

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            initStockEventMobile(result, drugStockFileEventArrayList)
        }

        return drugStockFileEventArrayList
    }

    def List<DrugFile> getDrugFileMobile(String clinicId) {
        List<Drug> drugs = Drug.findAllByActive(true)
        List<DrugFile> drugFileList = new ArrayList<>()
        for (Drug drug in drugs) {
            if (!drug.getStockList().isEmpty()) {
                DrugFile ficha = new DrugFile()
                List<DrugStockFileEvent> drugFileSummary = this.getDrugSumaryEventsMobile(clinicId, drug.id)
                List<DrugStockFileEvent> drugFileSummaryBatch = new ArrayList<>()
                for (Stock stock in drug.getStockList()) {
                    drugFileSummaryBatch.add(this.getDrugBatchSumaryEventsMobile(clinicId, drug.getStockList()[0].id).get(0))
                }
                ficha.setDrugFileSummary(drugFileSummary)
                ficha.setDrugFileSummaryBatch(drugFileSummaryBatch)
                ficha.setDrug(drug)
                ficha.setDrugId(drug.id)
                drugFileList.add(ficha)
            }
        }
        return drugFileList
    }


    private void initStockEventSummary(List result, ArrayList<DrugStockFileEvent> drugStockFileEventArrayList) {
        DateFormatSymbols symbols = new DateFormatSymbols(java.util.Locale.forLanguageTag("pt-BR"));
        String[] monthNames = symbols.getMonths();

        for (int i = 0; i < result.size(); i++) {
            DrugStockFileEvent drugStockFileEvent = new DrugStockFileEvent()

            drugStockFileEvent.moviment = String.valueOf(result[i][3])
            drugStockFileEvent.year = result[i][0]
            int monthNumber =Double.parseDouble(result[i][1]+"").intValue()
            String monthName = monthNames[monthNumber - 1];
            drugStockFileEvent.month =  monthName
            drugStockFileEvent.incomes =  Double.parseDouble(result[i][4]+"").longValue()


            drugStockFileEvent.outcomes = Double.parseDouble(result[i][5]+"").longValue()
            drugStockFileEvent.posetiveAdjustment = Double.parseDouble(result[i][6]+"").longValue()
            drugStockFileEvent.negativeAdjustment =Double.parseDouble(result[i][7]+"").longValue()
            drugStockFileEvent.loses = Double.parseDouble(result[i][8]+"").longValue()
             drugStockFileEvent.code = String.valueOf(result[i][10])
            drugStockFileEvent.stockId = String.valueOf(result[i][11])

            if (Utilities.listHasElements(drugStockFileEventArrayList as ArrayList<?>)) {
                    drugStockFileEvent.calculateBalance(drugStockFileEventArrayList.get(i -1).getBalance())

            } else {
                drugStockFileEvent.balance = ( drugStockFileEvent.incomes  + drugStockFileEvent.posetiveAdjustment) - ( drugStockFileEvent.outcomes + drugStockFileEvent.negativeAdjustment + drugStockFileEvent.loses )
            }

            drugStockFileEventArrayList.add(drugStockFileEvent)
        }

    }
    private void initStockEventBatch(List result, ArrayList<DrugStockFileEvent> drugStockFileEventArrayList) {
        for (int i = 0; i < result.size(); i++) {
            DrugStockFileEvent drugStockFileEvent = new DrugStockFileEvent()

            drugStockFileEvent.moviment = String.valueOf(result[i][2])


            drugStockFileEvent.eventDate = result[i][1] as Date
            drugStockFileEvent.incomes = Double.valueOf(String.valueOf(result[i][3])).longValue()
            drugStockFileEvent.outcomes = Double.valueOf(result[i][4]).longValue()
            drugStockFileEvent.posetiveAdjustment = Double.valueOf(String.valueOf(result[i][5])).longValue()
            drugStockFileEvent.negativeAdjustment = Double.valueOf(String.valueOf(result[i][6])).longValue()
            drugStockFileEvent.loses = Double.valueOf(String.valueOf(result[i][7])).longValue()
           // drugStockFileEvent.code = String.valueOf(result[i][11])
            drugStockFileEvent.stockId = String.valueOf(result[i][10])
            //drugStockFileEvent.notes = (Utilities.stringHasValue(String.valueOf(result[i][13])) && String.valueOf(result[i][13]) != "null") ? String.valueOf(result[i][13]) : ""

            if (Utilities.listHasElements(drugStockFileEventArrayList as ArrayList<?>)) {
                drugStockFileEvent.calculateBalance(drugStockFileEventArrayList.get(i- 1).getBalance())
            } else {
                drugStockFileEvent.balance = ( drugStockFileEvent.incomes  + drugStockFileEvent.posetiveAdjustment) - ( drugStockFileEvent.outcomes + drugStockFileEvent.negativeAdjustment + drugStockFileEvent.loses )
            }
            drugStockFileEventArrayList.add(drugStockFileEvent)
        }
    }
    
    private void initStockEventMobile(List result, ArrayList<DrugStockFileEvent> drugStockFileEventArrayList) {
        DrugStockFileEvent drugStockFileEvent = new DrugStockFileEvent()
        for (int i = 0; i < result.size(); i++) {
            drugStockFileEvent.moviment = String.valueOf(result[i][3])
            drugStockFileEvent.incomes = Long.valueOf(String.valueOf(result[i][5]))
            drugStockFileEvent.outcomes = Long.valueOf(String.valueOf(result[i][6]))
            drugStockFileEvent.posetiveAdjustment = Long.valueOf(String.valueOf(result[i][7]))
            drugStockFileEvent.negativeAdjustment = Long.valueOf(String.valueOf(result[i][8]))
            drugStockFileEvent.loses = Long.valueOf(String.valueOf(result[i][9]))
           // drugStockFileEvent.code = String.valueOf(result[i][11])
            drugStockFileEvent.stockId = String.valueOf(result[i][12])
           // drugStockFileEvent.notes = String.valueOf(result[i][13])
            drugStockFileEvent.calculateBalance(0)
        }
        drugStockFileEventArrayList.add(drugStockFileEvent)
    }


}
