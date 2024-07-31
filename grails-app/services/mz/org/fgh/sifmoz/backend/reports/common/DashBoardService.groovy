package mz.org.fgh.sifmoz.backend.reports.common

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.reports.patients.ActivePatientReport
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import mz.org.fgh.sifmoz.dashboard.ActivePatientPercentage
import mz.org.fgh.sifmoz.dashboard.DashboardServiceButton
import mz.org.fgh.sifmoz.dashboard.DispensesByAge
import mz.org.fgh.sifmoz.dashboard.DispensesByGender
import mz.org.fgh.sifmoz.dashboard.PatientsFirstDispenseByAge
import mz.org.fgh.sifmoz.dashboard.PatientsFirstDispenseByGender
import mz.org.fgh.sifmoz.dashboard.RegisteredPatientsByDispenseType
import mz.org.fgh.sifmoz.dashboard.StockAlert
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.math.RoundingMode

@Transactional
class DashBoardService {

    @Autowired
    SessionFactory sessionFactory

    def List<RegisteredPatientsByDispenseType> getRegisteredPatientByDispenseType(Date startDate, Date endDate, String clinicId, String serviceCode) {

        Clinic clinic = Clinic.findById(clinicId)

        String queryString = "select count(*) quantity, pat_disp.dispense_type dispense_type, pat_disp.month     " +
                "                from (     " +
                "                       select     " +
                "                               distinct p.id,     " +
                "                               psi.start_date,     " +
                "                               dt.code as dispense_type,     " +
                "                               cs.code as service,     " +
                "                               pk.pickup_date,     " +
                "                               (CASE     " +
                "                                   WHEN (EXTRACT(DAY FROM pk.pickup_date) >= 21) THEN EXTRACT(MONTH FROM (pk.pickup_date + interval '1 month'))     " +
                "                                   WHEN (EXTRACT(DAY FROM pk.pickup_date) <= 20) THEN EXTRACT(MONTH FROM pk.pickup_date)     " +
                "                               END) as month     " +
                "                       from patient_visit pv2  inner join patient p on pv2.patient_id = p.id     " +
                "                                               inner join patient_service_identifier psi on p.id = psi.patient_id     " +
                "                                               inner join episode e on e.patient_service_identifier_id  = psi .id     " +
                "                                               inner join patient_visit_details pvd on pvd.episode_id = e.id and pvd.patient_visit_id = pv2.id     " +
                "                                               inner join prescription pre on pre.id = pvd.prescription_id     " +
                "                                               inner join prescription_detail pd on pd.prescription_id = pre.id     " +
                "                                               inner join dispense_type dt on dt.id = pd.dispense_type_id     " +
                "                                               inner join clinical_service cs on psi.service_id = cs.id     " +
                "                                               inner join pack pk on pvd.pack_id = pk.id     " +
                "                       where     " +
                "                       pk.pickup_date BETWEEN :startDate  AND :endDate     " +
                "                           and cs.code = :service    " +
                "                           ) pat_disp     " +
                "                              " +
                "                group by pat_disp.dispense_type, pat_disp.month    " +
                "                order by pat_disp.month asc"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        List<Object[]> result = query.list()

        List<RegisteredPatientsByDispenseType> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {
            for (item in result) {
                registeredPatientsByDispenseTypeList.add(new RegisteredPatientsByDispenseType(String.valueOf(item[1]), Integer.valueOf(String.valueOf(item[0])), (int) Double.parseDouble(String.valueOf(item[2]))))
            }
        }
        return registeredPatientsByDispenseTypeList
    }


    def List<PatientsFirstDispenseByGender> getPatientsFirstDispenseByGender(Date startDate, Date endDate, String clinicId, String serviceCode) {

        Clinic clinic = Clinic.findById(clinicId)

        String queryString =
                """
                    select count(*) quantity, r1.month, r1.gender  
                    from (
                        select distinct p.id,
                            (CASE  
                            WHEN p.gender = 'Masculino' THEN 'Masculino'   
                            WHEN p.gender = 'Feminino' THEN 'Feminino'   
                            END) as gender,   
                            (CASE  
                            WHEN (EXTRACT(DAY FROM pv.visit_date) >= 21) THEN EXTRACT(MONTH FROM (pv.visit_date + interval '1 month'))  
                            WHEN (EXTRACT(DAY FROM pv.visit_date) <= 20) THEN EXTRACT(MONTH FROM pv.visit_date)  
                            END) as month  
                       from (
                           select distinct pat.id,
                                max(pk.pickup_date) pickupdate,
                                max(pk.id) packid,
                                pat.date_of_birth,
                                cs.code service_code
                           from patient_visit_details pvd
                           inner join pack pk on pk.id = pvd.pack_id
                           inner join episode ep on ep.id = pvd.episode_id
                           inner join prescription prc ON prc.id = pvd.prescription_id
                           inner join start_stop_reason ssr on ssr.id = ep.start_stop_reason_id
                           inner join prescription_detail pred on pred.prescription_id = prc.id
                           inner join dispense_type dt ON dt.id = pred.dispense_type_id
                           inner join patient_visit pv on pv.id = pvd.patient_visit_id
                           inner join patient pat on pat.id = pv.patient_id
                           inner join patient_service_identifier psi on psi.id = ep.patient_service_identifier_id
                           inner join clinical_service cs ON cs.id = psi.service_id
                           inner join clinic c on c.id = ep.clinic_id
                           where Date(pk.pickup_date) BETWEEN :startDate AND :endDate
                           AND cs.code = :service
                           AND c.id = :clinicId
                           AND (prc.patient_type = 'N/A' OR prc.patient_type IS null OR prc.patient_type = 'Inicio') 
                           AND dt.code = 'DM'
                           AND ssr.code = 'NOVO_PACIENTE'
                           AND (Date(ep.episode_date) <= Date(pk.pickup_date) AND pk.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                           group by 1,4,5
                           ORDER BY 1 asc
                       ) lastPack
                    inner join pack pack2 ON pack2.id = lastPack.packid
                    inner join patient_visit_details pvd ON pvd.pack_id = pack2.id
                    inner join episode ep on ep.id = pvd.episode_id
                    inner join start_stop_reason ssr on ssr.id = ep.start_stop_reason_id
                    inner join patient_service_identifier psi on psi.id = ep.patient_service_identifier_id
                    inner join clinical_service cs ON cs.id = psi.service_id
                    inner join patient_visit pv on pv.id = pvd.patient_visit_id
                    inner join patient p ON p.id = psi.patient_id
                    inner join clinic c on c.id = ep.clinic_id
                    where cs.code = :service 
                    AND c.id = :clinicId
                    AND ssr.code = 'NOVO_PACIENTE'
                    AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days')) 
                    order by 3
                    ) r1
                    group by 2,3  
                    order by 2 asc
                """

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        query.setParameter("clinicId", clinic.id)
        List<Object[]> result = query.list()

        List<PatientsFirstDispenseByGender> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                if (item[1] != null && item[2] != null)
                    registeredPatientsByDispenseTypeList.add(new PatientsFirstDispenseByGender(Integer.valueOf(String.valueOf(item[0])), (int) Double.parseDouble(String.valueOf(item[1])), String.valueOf(item[2])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }


    def List<PatientsFirstDispenseByAge> getPatientsFirstDispenseByAge(Date startDate, Date endDate, String clinicId, String serviceCode) {

        Clinic clinic = Clinic.findById(clinicId)

        String queryString = 
                """
                    select count(*) quantity, r1.month, r1.faixa  
                    from (
                        select distinct p.id,
                            (CASE  
                            WHEN (date_part('year', age(p.date_of_birth)) < 15) THEN 'MENOR'   
                            WHEN (date_part('year', age(p.date_of_birth)) >= 15) THEN 'ADULTO'   
                            END) as faixa,   
                            (CASE  
                            WHEN (EXTRACT(DAY FROM pv.visit_date) >= 21) THEN EXTRACT(MONTH FROM (pv.visit_date + interval '1 month'))  
                            WHEN (EXTRACT(DAY FROM pv.visit_date) <= 20) THEN EXTRACT(MONTH FROM pv.visit_date)  
                            END) as month  
                       from (
                           select distinct pat.id,
                                max(pk.pickup_date) pickupdate,
                                max(pk.id) packid,
                                pat.date_of_birth,
                                cs.code service_code
                           from patient_visit_details pvd
                           inner join pack pk on pk.id = pvd.pack_id
                           inner join episode ep on ep.id = pvd.episode_id
                           inner join prescription prc ON prc.id = pvd.prescription_id
                           inner join start_stop_reason ssr on ssr.id = ep.start_stop_reason_id
                           inner join prescription_detail pred on pred.prescription_id = prc.id
                           inner join dispense_type dt ON dt.id = pred.dispense_type_id
                           inner join patient_visit pv on pv.id = pvd.patient_visit_id
                           inner join patient pat on pat.id = pv.patient_id
                           inner join patient_service_identifier psi on psi.id = ep.patient_service_identifier_id
                           inner join clinical_service cs ON cs.id = psi.service_id
                           inner join clinic c on c.id = ep.clinic_id
                           where Date(pk.pickup_date) BETWEEN :startDate AND :endDate
                           AND cs.code = :service
                           AND c.id = :clinicId
                           AND (prc.patient_type = 'N/A' OR prc.patient_type IS null OR prc.patient_type = 'Inicio') 
                           AND dt.code = 'DM'
                           AND ssr.code = 'NOVO_PACIENTE'
                           AND (Date(ep.episode_date) <= Date(pk.pickup_date) AND pk.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                           group by 1,4,5
                           ORDER BY 1 asc
                       ) lastPack
                    inner join pack pack2 ON pack2.id = lastPack.packid
                    inner join patient_visit_details pvd ON pvd.pack_id = pack2.id
                    inner join episode ep on ep.id = pvd.episode_id
                    inner join start_stop_reason ssr on ssr.id = ep.start_stop_reason_id
                    inner join patient_service_identifier psi on psi.id = ep.patient_service_identifier_id
                    inner join clinical_service cs ON cs.id = psi.service_id
                    inner join patient_visit pv on pv.id = pvd.patient_visit_id
                    inner join patient p ON p.id = psi.patient_id
                    inner join clinic c on c.id = ep.clinic_id
                    where cs.code = :service 
                    AND c.id = :clinicId
                    AND ssr.code = 'NOVO_PACIENTE'
                    AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days')) 
                    order by 3
                    ) r1
                    group by 2,3  
                    order by 2 asc
                """

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        query.setParameter("clinicId", clinic.id)
        List<Object[]> result = query.list()

        List<PatientsFirstDispenseByAge> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                if (item[1] != null && item[2] != null)
                    registeredPatientsByDispenseTypeList.add(new PatientsFirstDispenseByAge(Integer.valueOf(String.valueOf(item[0])), (int) Double.parseDouble(String.valueOf(item[1])), String.valueOf(item[2])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }

    def List<ActivePatientPercentage> getActivePatientPercentage(Date startDate, Date endDate, String clinicId, String serviceCode) {


        if(endDate >= new Date())
            endDate = new Date()

        String queryString = "WITH ResultTable AS (    " +
                "    SELECT    " +
                "        p.id AS patient_id,    " +
                "        MAX(pk.pickup_date) AS pickUpDate,    " +
                "        COUNT(*) AS quantity    " +
                "    FROM patient_visit_details AS pvd    " +
                "    INNER JOIN pack pk ON pk.id = pvd.pack_id    " +
                "    INNER JOIN patient_visit pv ON pv.id = pvd.patient_visit_id    " +
                "    INNER JOIN patient p ON p.id = pv.patient_id    " +
                "    INNER JOIN episode ep ON ep.id = pvd.episode_id     " +
                "    INNER JOIN start_stop_reason ssr ON ssr.id = ep.start_stop_reason_id    " +
                "    INNER JOIN patient_service_identifier psi ON p.id = psi.patient_id    " +
                "    INNER JOIN clinical_service cs ON cs.id = psi.service_id    " +
                "    WHERE pk.pickup_date BETWEEN :startDate AND :endDate AND pk.next_pick_up_date + INTERVAL '3 DAY' >= :endDate   " +
                "        AND ssr.code IN ('NOVO_PACIENTE','INICIO_CCR','TRANSFERIDO_DE','REINICIO_TRATAMETO','MANUNTENCAO','OUTRO','VOLTOU_REFERENCIA', 'REFERIDO_DC')   " +
                "        and cs.code = :service and ep.clinic_id = :clinicId " +
                "    GROUP BY p.id    " +
                ")    " +
                "select DISTINCT    " +
                "    count(*) / SUM(COUNT(*)) OVER () * 100 percent, p.gender, count(*) quantity   " +
                "FROM patient p    " +
                "INNER JOIN patient_service_identifier psi ON p.id = psi.patient_id   " +
                "INNER JOIN clinical_service cs ON cs.id = psi.service_id and cs.code = :service " +
                "INNER JOIN ResultTable r ON p.id = r.patient_id    " +
                "GROUP BY p.gender order by p.gender asc"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        query.setParameter("clinicId", clinicId)
        List<Object[]> result = query.list()

        List<ActivePatientPercentage> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                registeredPatientsByDispenseTypeList.add(new ActivePatientPercentage((int) Double.parseDouble(String.valueOf(item[2])), Double.parseDouble(String.valueOf(item[0])), String.valueOf(item[1])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }


    def List<DispensesByAge> getDispenseByAge(Date startDate, Date endDate, String clinicId, String serviceCode) {

        Clinic clinic = Clinic.findById(clinicId)

        String queryString = "select  dt.description  dispense_type,   " +
                "SUM(CASE WHEN date_part('year', age(pat.date_of_birth)) >= 15 THEN 1 ELSE 0 END) ADULTO,  " +
                "SUM(CASE WHEN date_part('year', age(pat.date_of_birth)) < 15 THEN 1 ELSE 0 END) MENOR  " +
                "from pack p inner join patient_visit_details pvd on p.id = pvd.pack_id   " +
                "      inner join patient_visit pv on pv.id = pvd.patient_visit_id   " +
                "      inner join episode e on e.id = pvd.episode_id   " +
                "      inner join patient_service_identifier psi on psi.id = e.patient_service_identifier_id   " +
                "      inner join clinical_service cs on cs.id = psi.service_id   " +
                "      inner join prescription pre on pre.id = pvd.prescription_id   " +
                "      inner join prescription_detail pd on pd.prescription_id = pre.id   " +
                "      inner join dispense_type dt ON dt.id = pd.dispense_type_id   " +
                "      inner join patient pat on pat.id = pv.patient_id   " +
                "where (p.pickup_date between  :startDate and :endDate)  " +
                "    and cs.code = :service  " +
                "    and p.clinic_id = :clinic " +
                "group by dt.description"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        query.setParameter("clinic", clinic.id)
        List<Object[]> result = query.list()

        List<DispensesByAge> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                registeredPatientsByDispenseTypeList.add(new DispensesByAge((int) Double.parseDouble(String.valueOf(item[1])), (int) Double.parseDouble(String.valueOf(item[2])), String.valueOf(item[0])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }

    def List<DispensesByGender> getDispensesByGender(Date startDate, Date endDate, String clinicId, String serviceCode) {

        Clinic clinic = Clinic.findById(clinicId)

        String queryString = "select  dt.description  dispense_type,   " +
                "SUM(CASE WHEN pat.gender = 'Masculino' THEN 1 ELSE 0 END) masculino,  " +
                "SUM(CASE WHEN pat.gender = 'Feminino' THEN 1 ELSE 0 END) feminino  " +
                "from pack p inner join patient_visit_details pvd on p.id = pvd.pack_id   " +
                "      inner join patient_visit pv on pv.id = pvd.patient_visit_id   " +
                "      inner join episode e on e.id = pvd.episode_id   " +
                "      inner join patient_service_identifier psi on psi.id = e.patient_service_identifier_id   " +
                "      inner join clinical_service cs on cs.id = psi.service_id   " +
                "      inner join prescription pre on pre.id = pvd.prescription_id   " +
                "      inner join prescription_detail pd on pd.prescription_id = pre.id   " +
                "      inner join dispense_type dt ON dt.id = pd.dispense_type_id   " +
                "      inner join patient pat on pat.id = pv.patient_id   " +
                "where (p.pickup_date  between :startDate and :endDate)  " +
                "    and cs.code = :service  " +
                "    and p.clinic_id = :clinic " +
                "group by dt.description"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("service", serviceCode)
        query.setParameter("clinic", clinic.id)
        List<Object[]> result = query.list()

        List<DispensesByGender> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                registeredPatientsByDispenseTypeList.add(new DispensesByGender((int) Double.parseDouble(String.valueOf(item[1])), (int) Double.parseDouble(String.valueOf(item[2])), String.valueOf(item[0])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }


    def List<StockAlert> getStockAlert(String clinicId, String serviceCode) {

        String queryString = "  SELECT" +
                "  stock_consuption.id, " +
                "  stock_consuption.drug," +
                "  stock_consuption.balance," +
                "  stock_consuption.consuption / 3 AS avg_consuption, " +
                "  CASE " +
                "    WHEN stock_consuption.consuption = 0 THEN 'Sem Consumo' " +
                "    WHEN stock_consuption.balance > (stock_consuption.consuption / 3) THEN 'Acima do Consumo Máximo' " +
                "    WHEN stock_consuption.balance < (stock_consuption.consuption / 3) THEN 'Ruptura de Stock' " +
                "    ELSE 'Stock Normal' " +
                "  END AS state " +
                "FROM " +
                "(SELECT " +
                " d.id as id, " +
                "d.name as drug, " +
                "(SUM(svw.incomes) - SUM(svw.outcomes) + SUM(svw.positiveadjustment) - SUM(svw.negativeadjustment) - SUM(svw.losses)) AS balance, " +
                "    ( " +
                "      SELECT COALESCE(SUM(pds.quantity_supplied), 0) " +
                "      FROM packaged_drug pd " +
                "      INNER JOIN pack p ON pd.pack_id = p.id " +
                "      INNER JOIN packaged_drug_stock pds ON pds.packaged_drug_id = pd.id " +
                "      WHERE pd.drug_id = d.id  " +
                "    ) AS consuption " +
                " " +
                "FROM drug_stock_summary_vw svw " +
                "INNER JOIN drug d ON svw.drug_id = d.id " +
                " inner join clinical_service cs on cs.id = d.clinical_service_id   " +
                " where cs.code = :service  " +
                " and cs.code = :service " +
                " and svw.clinic_id = :clinicId  " +
                " GROUP BY d.id) as  stock_consuption";

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("service", serviceCode)
        query.setParameter("clinicId", clinicId)
        List<Object[]> result = query.list()

        List<StockAlert> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                BigDecimal avrg = new BigDecimal(Double.parseDouble(String.valueOf(item[3])))
                registeredPatientsByDispenseTypeList.add(new StockAlert(String.valueOf(item[0]), String.valueOf(item[1]), (int) Double.parseDouble(String.valueOf(item[2])), avrg.setScale(2, RoundingMode.HALF_UP), String.valueOf(item[4])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }


    def List<DashboardServiceButton> getDashboardServiceButton(Date startDate, Date endDate, String clinicId) {

        Clinic clinic = Clinic.findById(clinicId)

        if(endDate >= new Date())
            endDate = new Date()

        String queryString = "WITH ResultTable AS (  " +
                "SELECT  " +
                "p.id AS patient_id,  " +
                "MAX(pk.pickup_date) AS pickUpDate,  " +
                "COUNT(*) AS quantity  " +
                "FROM patient_visit_details AS pvd  " +
                "    INNER JOIN pack pk ON pk.id = pvd.pack_id  " +
                "    INNER JOIN patient_visit pv ON pv.id = pvd.patient_visit_id  " +
                "    INNER JOIN patient p ON p.id = pv.patient_id  " +
                "    INNER JOIN episode ep ON ep.id = pvd.episode_id   " +
                "    INNER JOIN start_stop_reason ssr ON ssr.id = ep.start_stop_reason_id  " +
                "    INNER JOIN patient_service_identifier psi ON p.id = psi.patient_id  " +
                "    INNER JOIN clinical_service cs ON cs.id = psi.service_id  " +
                "    WHERE pk.pickup_date BETWEEN :startDate AND :endDate AND pk.next_pick_up_date + INTERVAL '3 DAY' >= :endDate  " +
                "        AND pk.clinic_id = :clinic  " +
                "        AND ssr.code IN ('NOVO_PACIENTE','INICIO_CCR','TRANSFERIDO_DE','REINICIO_TRATAMETO','MANUNTENCAO','OUTRO','VOLTOU_REFERENCIA', 'REFERIDO_DC')  " +
                "    GROUP BY p.id  " +
                ")  " +
                "select DISTINCT  " +
                "    COUNT(*) AS quantity,  " +
                "    cs.code as service  " +
                "FROM patient p  " +
                "INNER JOIN patient_service_identifier psi ON p.id = psi.patient_id  " +
                "INNER JOIN clinical_service cs ON cs.id = psi.service_id  " +
                "INNER JOIN ResultTable r ON p.id = r.patient_id  " +
                "GROUP BY cs.code "

        /* String queryString ="select count(*) quantity, cs.code  service  " +
                 "from patient_service_identifier psi inner join clinical_service cs on psi.service_id = cs.id   " +
                 "where psi.clinic_id = :clinic and psi.start_date <= :endDate " +
                 "group by cs.code " */

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("clinic", clinic.id)
        List<Object[]> result = query.list()

        List<DashboardServiceButton> registeredPatientsByDispenseTypeList = new ArrayList<>()

        if (Utilities.listHasElements(result as ArrayList<?>)) {

            for (item in result) {
                registeredPatientsByDispenseTypeList.add(new DashboardServiceButton((int) Double.parseDouble(String.valueOf(item[0])), String.valueOf(item[1])))
            }
        }
        return registeredPatientsByDispenseTypeList
    }
}
