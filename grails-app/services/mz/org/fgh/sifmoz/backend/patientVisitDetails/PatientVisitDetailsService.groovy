package mz.org.fgh.sifmoz.backend.patientVisitDetails

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation.DrugQuantityTemp
import mz.org.fgh.sifmoz.backend.stock.Stock
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import java.util.stream.Collectors

@Transactional
@Service(PatientVisitDetails)
abstract class PatientVisitDetailsService implements IPatientVisitDetailsService {

    @Autowired
    SessionFactory sessionFactory

    @Override
    List<PatientVisitDetails> getAllByClinicId(String clinicId, int offset, int max) {
        return PatientVisitDetails.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

    @Override
    List<PatientVisitDetails> getAllLastVisitOfClinic(String clinicId, int offset, int max) {
        Session session = sessionFactory.getCurrentSession()

        String queryString = "select *  " +
                "from patient_last_visit_details_vw  " +
                "where clinic_id = :clinic offset :offset limit :max "


        def query = session.createSQLQuery(queryString).addEntity(PatientVisitDetails.class)
        query.setParameter("clinic", clinicId)
        query.setParameter("offset", offset)
        query.setParameter("max", max)
        List<PatientVisitDetails> result = query.list()
        return result
    }

    @Override
    List<PatientVisitDetails> getAllByEpisodeId(String episodeId, int offset, int max) {
        def patientVisitDetails = PatientVisitDetails.findAllByEpisode(Episode.findById(episodeId))
        return patientVisitDetails
    }

    @Override
    PatientVisitDetails getByPack(Pack pack) {
        return PatientVisitDetails.findByPack(pack)
    }

    List<Object[]> getReferralPatients(String clinicId, Date startDate, Date endDate, String refIdaOuVolta) {
        def queryString =
                "with ResultTable AS( " +
                "SELECT p.id as patient_id, e.id as episode_id, psi.value as nid, e.referral_clinic_id referral_clinic_id, e.notes, MAX(e.episode_date) as last_episode_date " +
                "  FROM episode e " +
                "  inner join patient_service_identifier psi on e.patient_service_identifier_id = psi.id " +
                "  inner join clinical_service cs on cs.id =  psi.service_id and  cs.code = 'TARV' " +
                "  inner join patient p on p.id = psi.patient_id  " +
                "  inner join start_stop_reason ssr on ssr.id = e.start_stop_reason_id " +
                "  where ssr.code = :refIdaOuVolta and e.episode_date between :startDate and :endDate and psi.clinic_id = :clinicId " +
                "  group by p.id, e.episode_date, e.id, psi.value, e.clinic_id, e.notes " +
                "  order by e.episode_date desc  " +
                ") " +
                "select distinct on (p2.id)  " +
                "r1.last_episode_date,  " +
                "r1.nid,  " +
                "p2.first_names || ' ' || last_names as Pat_name, " +
                "EXTRACT(year FROM age(:endDate, p2.date_of_birth)) as idade, " +
                "r2.last_presc as last_prescription_date, " +
                "tr.description as regime, " +
                "tl.description as linha, " +
                "dt.code as dispense_type, " +
                "pack.pickup_date, " +
                "pack.next_pick_up_date, " +
                "r1.last_episode_date as referrence_date, " +
                "cl.clinic_name as referral_pharmacy, " +
                "r1.notes, " +
                "p2.cellphone || '  ' || p2.alternative_cellphone as contact, " +
                "r1.episode_id " +
                "from patient p2  " +
                "inner join ResultTable r1 on r1.patient_id = p2.id " +
                "inner join patient_visit_details pvd on r1.episode_id = pvd.episode_id " +
                "inner join prescription pre on pre.id = pvd.prescription_id " +
                "inner join ( " +
                "select pat.id as pat_id, max(pre.prescription_date) as last_presc " +
                "from patient pat " +
                "inner join patient_visit pv on pv.patient_id = pat.id " +
                "inner join patient_visit_details pvd on pv.id = pvd.patient_visit_id  " +
                "inner join prescription pre on pvd.prescription_id = pre.id  " +
                "group by pat.id, pre.prescription_date " +
                "order by pre.prescription_date) r2 on r2.pat_id = p2.id and r2.last_presc = pre.prescription_date " +
                "inner join prescription_detail pd on pd.prescription_id = pre.id " +
                "inner join therapeutic_regimen tr on tr.id = pd.therapeutic_regimen_id " +
                "inner join therapeutic_line tl on tl.id = pd.therapeutic_line_id " +
                "inner join dispense_type dt on pd.dispense_type_id = dt.id " +
                "inner join pack pack on pack.id = pvd.pack_id " +
                "left join clinic cl on cl.id = r1.referral_clinic_id "

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("clinicId", clinicId)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("refIdaOuVolta", refIdaOuVolta)

        List<Object[]> list = query.list()
        return list
    }
    
    List<Object> getAbsentReferredPatientsByClinicalServiceAndClinicOnPeriod(String clinicId, Date startDate, Date endDate) {
        def queryString =
                "with ResultTable AS( " +
                        "SELECT p.id as patient_id, e.id as episode_id, psi.value as nid, e.referral_clinic_id referral_clinic_id, e.notes, MAX(e.episode_date) as last_episode_date " +
                        "  FROM episode e " +
                        "  inner join patient_service_identifier psi on e.patient_service_identifier_id = psi.id " +
                        "  inner join clinical_service cs on cs.id =  psi.service_id and  cs.code = 'TARV' " +
                        "  inner join patient p on p.id = psi.patient_id  " +
                        "  inner join start_stop_reason ssr on ssr.id = e.start_stop_reason_id " +
                        "  where ssr.code = 'REFERIDO_PARA' and e.episode_date between :startDate and :endDate and psi.clinic_id = :clinicId " +
                        "  group by p.id, e.episode_date, e.id, psi.value, e.referral_clinic_id, e.notes " +
                        "  order by e.episode_date desc  " +
                        ") " +
                        "select distinct on (p2.id)  " +
                        "r1.last_episode_date,  " +
                        "r1.nid,  " +
                        "p2.first_names || ' ' || last_names as Pat_name, " +
                        "EXTRACT(year FROM age(:endDate, p2.date_of_birth)) as idade, " +
                        "r2.last_presc as last_prescription_date, " +
                        "tr.description as regime, " +
                        "tl.description as linha, " +
                        "dt.code as dispense_type, " +
                        "pack.pickup_date, " +
                        "pack.next_pick_up_date, " +
                        "r1.last_episode_date as referrence_date, " +
                        "cl.clinic_name as referral_pharmacy, " +
                        "r1.notes, " +
                        "p2.cellphone || '  ' || p2.alternative_cellphone as contact, " +
                        "r1.episode_id, " +
                        "(select pk4.pickup_date from patient_visit_details pvd2   " +
                        "inner join pack pk4 on pvd2.pack_id = pk4.id " +
                        "inner join patient_visit pv2 on pvd2.patient_visit_id = pv2.id " +
                        "inner join episode ep3 on pvd2.episode_id = ep3.id " +
                        "inner join patient_service_identifier psi3 on ep3.patient_service_identifier_id = psi3.id " +
                        "inner join clinical_service s3 on psi3.service_id = s3.id " +
                        "where p2.id = psi3.patient_id and s3.code = 'TARV' and pk4.pickup_date > pk4.next_pick_up_date and pk4.pickup_date <= :endDate) as returnedPickUp " +
                        "from patient p2  " +
                        "inner join ResultTable r1 on r1.patient_id = p2.id " +
                        "inner join patient_visit_details pvd on r1.episode_id = pvd.episode_id " +
                        "inner join prescription pre on pre.id = pvd.prescription_id " +
                        "inner join ( " +
                        "select pat.id as pat_id, max(pre.prescription_date) as last_presc " +
                        "from patient pat " +
                        "inner join patient_visit pv on pv.patient_id = pat.id " +
                        "inner join patient_visit_details pvd on pv.id = pvd.patient_visit_id  " +
                        "inner join prescription pre on pvd.prescription_id = pre.id  " +
                        "group by pat.id, pre.prescription_date " +
                        "order by pre.prescription_date) r2 on r2.pat_id = p2.id and r2.last_presc = pre.prescription_date " +
                        "inner join prescription_detail pd on pd.prescription_id = pre.id " +
                        "inner join therapeutic_regimen tr on tr.id = pd.therapeutic_regimen_id " +
                        "inner join therapeutic_line tl on tl.id = pd.therapeutic_line_id " +
                        "inner join dispense_type dt on pd.dispense_type_id = dt.id " +
                        "inner join pack pack on pack.id = pvd.pack_id and pack.next_pick_up_date >= :startDate and pack.next_pick_up_date <= :endDate and DATE(pack.next_pick_up_date) + interval '3 DAY' <= :endDate " +
                        "left join clinic cl on cl.id = r1.referral_clinic_id"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("clinicId", clinicId)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)

        List<Object[]> list = query.list()
        return list
    }

    @Override
    List<PatientVisitDetails> getARVDailyReport(String clinicId, Date startDate, Date endDate, String clincalServiceId) {
        def queryString = "WITH ResultTable AS ( " +
                "SELECT pvd.id as r_pvd_id, d.name as r_drug_name, SUM(pd.quantity_supplied) as r_quantity_drugs" +
                "    FROM patient_visit_details  pvd  " +
                "    INNER JOIN pack pack ON pvd.pack_id = pack.id  " +
                "    INNER JOIN packaged_drug pd ON pack.id = pd.pack_id  " +
                "    INNER JOIN drug d ON pd.drug_id = d.id  " +
                "    WHERE " +
                "    ((Date(pack.pickup_date) BETWEEN :startDate AND :endDate) OR  " +
                "     pg_catalog.date(pack.pickup_date) < :startDate and pg_catalog.date(pack.next_pick_up_date) > :endDate AND  " +
                "     DATE(pack.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) >= :startDate  " +
                "     and DATE(pack.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) <= :endDate) " +
                "   AND pack.clinic_id = :clinicId " +
                "    GROUP BY pvd.id, d.name" +
                ")                 " +
                " select   " +
                "     psi.value,  " +
                "     pat.first_names ,  " +
                "     pat.middle_names,  " +
                "     pat.last_names ,  " +
                "     pr.patient_type,  " +
                "     pat.date_of_birth,  " +
                "     CASE   " +
                "      WHEN dt.code = 'DT' THEN  " +
                "        CASE WHEN  pack2.pickup_date >= :startDate THEN 'DT'  " +
                "           ELSE 'DT-TRANSPORTE' " +
                "             END  " +
                "         WHEN dt.code = 'DS' THEN  " +
                "           CASE WHEN  pack2.pickup_date >= :startDate THEN 'DS'  " +
                "             ELSE 'DS-TRANSPORTE' " +
                "               END  " +
                "            WHEN dt.code = 'DM' THEN  " +
                "            CASE WHEN  pack2.pickup_date >= :startDate THEN 'DM'  " +
                "              ELSE 'DM-TRANSPORTE'  " +
                "              END  " +
                "         END AS tipodispensa, " +
                "     tl.description as description2,  " +
                "     pack2.pickup_date,  " +
                "     pack2.next_pick_up_date,  " +
                "     tr.description as description3,  " +
                "     pack2.id as id1,  " +
                "     ssr.reason,  " +
                "     cs.code,  " +
                "     ssr.is_start_reason, " +
                "     pvd.id as id2," +
                "     r.r_drug_name," +
                "     r.r_quantity_drugs" +
                "     from patient_visit_details  pvd  " +
                "     inner join ResultTable r on pvd.id = r.r_pvd_id" +
                "     inner join pack pack2 on (pvd.pack_id = pack2.id)  " +
                "     inner join prescription pr on (pvd.prescription_id = pr.id)  " +
                "     inner join prescription_detail prd on (pr.id = prd.prescription_id)  " +
                "     inner join dispense_type dt on (prd.dispense_type_id = dt.id)  " +
                "     inner join episode ep on (ep.id = pvd.episode_id)  " +
                "     inner join start_stop_reason ssr on (ep.start_stop_reason_id = ssr.id)  " +
                "     inner join patient_Service_identifier psi on (ep.patient_Service_identifier_id = psi.id)  " +
                "     inner join patient pat on (psi.patient_id = pat.id)  " +
                "     inner join therapeutic_regimen tr on (prd.therapeutic_regimen_id = tr.id)  " +
                "     inner join therapeutic_line tl on (prd.therapeutic_line_id = tl.id)  " +
                "     inner join clinical_service cs on (psi.service_id = cs.id)  " +
                "     where ((Date(pack2.pickup_date) BETWEEN :startDate AND :endDate) " +
                "     AND DATE(pack2.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) >= :startDate  " +
                "     AND DATE(pack2.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) <= :endDate)  " +
                "     AND pack2.clinic_id = :clinicId  " +
                "     AND psi.service_id = :clincalServiceId"


        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("clinicId", clinicId)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("clincalServiceId", clincalServiceId)

        List<Object[]> list = query.list()
        return list

    }

    List<DrugQuantityTemp> getProducts(String patientVisitDetailId, String clinicId) {
        List<DrugQuantityTemp> listDrugTemp = new ArrayList<>()

        def list = Stock.executeQuery("SELECT d.name, SUM(pd.quantity_supplied) " +
                "FROM patient_visit_details  pvd " +
                "INNER JOIN pack p ON pvd.pack_id = p.id " +
                "INNER JOIN packaged_drug pd ON p.id = pd.pack_id " +
                "INNER JOIN drug d ON pd.drug_id = d.id " +
                "WHERE pvd.id = :patientVisitDetailId AND p.clinic_id = :clinicId " +
                "GROUP BY d.name"
                , [patientVisitDetailId: patientVisitDetailId, clinicId: clinicId])
        list.each { item ->
            def drugName = item[0]
            def quantity = item[1]
            listDrugTemp.add(new DrugQuantityTemp(String.valueOf(drugName), Double.valueOf(quantity).longValue()))
        }
        return listDrugTemp
    }

    @Override
    PatientVisitDetails getLastVisitByEpisodeId(String episodeId) {
        def patientVisitDetails = PatientVisitDetails.findAllByEpisode(Episode.findById(episodeId), [sort: ['patientVisit.visitDate': 'desc']])
        return patientVisitDetails.get(0)
    }

    @Override
    PatientVisitDetails getLastByEpisodeId(String episodeId) {
        def list = PatientVisitDetails.executeQuery("select pvd " +
                "from PatientVisitDetails pvd " +
                "inner join pvd.patientVisit pv " +
                "inner join pvd.episode e " +
                "where e = :episode " +
                "order by pv.visitDate desc",
                [episode: Episode.findById(episodeId)])

        if (list == null || list.size() <= 0) return null
        return list.get(0)
    }

    List<PatientVisitDetails> getAllByPatientId(String patientId) {
        List<PatientVisit> patientVisitList = PatientVisit.findAllByPatient(Patient.findById(patientId))
        List<PatientVisitDetails> patientVisitDetailsList = new ArrayList<>();
        patientVisitList.each {it ->
            it.patientVisitDetails.each {pvd ->
                patientVisitDetailsList.add(pvd)
            }

        }
        print(patientVisitDetailsList.size())
        return patientVisitDetailsList
    }

    // NOVOS REPORTS
    @Override
    List<PatientVisitDetails> getTPTDailyReport(String clinicId, Date startDate, Date endDate, String clinicalServiceId) {
        def queryString =
                """

                WITH ResultTable AS ( 
                SELECT pvd.id as r_pvd_id, d.name as r_drug_name, SUM(pd.quantity_supplied) as r_quantity_drugs
                    FROM patient_visit_details  pvd  
                    INNER JOIN pack pack ON pvd.pack_id = pack.id  
                    INNER JOIN packaged_drug pd ON pack.id = pd.pack_id  
                    INNER JOIN drug d ON pd.drug_id = d.id  
                    WHERE 
                    ((Date(pack.pickup_date) BETWEEN :startDate AND :endDate) OR  
                     pg_catalog.date(pack.pickup_date) < :startDate and pg_catalog.date(pack.next_pick_up_date) > :endDate AND  
                     DATE(pack.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) >= :startDate  
                     and DATE(pack.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) <= :endDate) 
                   AND pack.clinic_id = :clinicId 
                    GROUP BY pvd.id, d.name
                )                 
                 select   
                     psi.value,  
                     pat.first_names ,  
                     pat.middle_names,  
                     pat.last_names ,  
                     CASE 
                       WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Inicio') 
                            AND (ssr.code = 'NOVO_PACIENTE' OR ssr.code = 'INICIO_CCR')
                            AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                        THEN 'Inicio'
                        ELSE
                            CASE
                                WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Transfer de')
                                    AND ssr.code = 'TRANSFERIDO_DE'
                                    AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                                THEN 'Transfer de'
                                ELSE
                                    CASE
                                        WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Reiniciar')
                                            AND ssr.code = 'REINICIO_TRATAMETO'
                                            AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                                        THEN 'Reiniciar'
                                        ELSE
                                            CASE
                                                WHEN (ssr.code = 'TRANSITO' OR ssr.code = 'INICIO_MATERNIDADE')
                                                    AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '90 days'))
                                                THEN 'Trânsito'
                                                ELSE
                                                    CASE
                                                        WHEN ssr.code = 'TERMINO_DO_TRATAMENTO'
                                                            AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                                                        THEN 'Termino do Tratamento'
                                                        ELSE
                                                            'Manutenção'
                                                    END
                                            END
                                    END
                            END
                        END as patient_type, 
                     pat.date_of_birth,  
                     CASE   
                      WHEN dt.code = 'DT' THEN  
                        CASE WHEN  pack2.pickup_date >= :startDate THEN 'DT'  
                           ELSE 'DT-TRANSPORTE' 
                             END  
                         WHEN dt.code = 'DS' THEN  
                           CASE WHEN  pack2.pickup_date >= :startDate THEN 'DS'  
                             ELSE 'DS-TRANSPORTE'
                               END  
                            WHEN dt.code = 'DM' THEN 
                            CASE WHEN  pack2.pickup_date >= :startDate THEN 'DM' 
                              ELSE 'DM-TRANSPORTE' 
                              END  
                         END AS tipodispensa, 
                     tl.description as description2, 
                     pack2.pickup_date,  
                     pack2.next_pick_up_date,  
                     tr.description as description3,  
                     pack2.id as id1,  
                     ssr.reason,  
                     cs.code,  
                     ssr.is_start_reason,
                     pvd.id as id2,
                     r.r_drug_name,
                     r.r_quantity_drugs
                     from patient_visit_details  pvd  
                     inner join ResultTable r on pvd.id = r.r_pvd_id
                     inner join pack pack2 on (pvd.pack_id = pack2.id)  
                     inner join prescription pr on (pvd.prescription_id = pr.id)  
                     inner join prescription_detail prd on (pr.id = prd.prescription_id)  
                     inner join dispense_type dt on (prd.dispense_type_id = dt.id)  
                     inner join episode ep on (ep.id = pvd.episode_id)  
                     inner join start_stop_reason ssr on (ep.start_stop_reason_id = ssr.id)  
                     inner join patient_Service_identifier psi on (ep.patient_Service_identifier_id = psi.id)  
                     inner join patient pat on (psi.patient_id = pat.id)  
                     inner join therapeutic_regimen tr on (prd.therapeutic_regimen_id = tr.id) 
                     inner join therapeutic_line tl on (prd.therapeutic_line_id = tl.id) 
                     inner join clinical_service cs on (psi.service_id = cs.id) 
                     where ((Date(pack2.pickup_date) BETWEEN :startDate AND :endDate) 
                     AND DATE(pack2.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) >= :startDate 
                     and DATE(pack2.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) <= :endDate) 
                     and pack2.clinic_id = :clinicId
                     and psi.service_id = :clinicalServiceId 
                """
        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("clinicId", clinicId)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("clinicalServiceId", clinicalServiceId)

        List<Object[]> list = query.list()
        return list
    }

    @Override
    List<PatientVisitDetails> getPREPDailyReport(String clinicId, Date startDate, Date endDate, String clinicalServiceId) {
        def queryString =
                """
                WITH ResultTable AS ( 
                    SELECT pvd.id as r_pvd_id, d.name as r_drug_name, SUM(pd.quantity_supplied) as r_quantity_drugs
                    FROM patient_visit_details  pvd  
                    INNER JOIN pack pack ON pvd.pack_id = pack.id  
                    INNER JOIN packaged_drug pd ON pack.id = pd.pack_id  
                    INNER JOIN drug d ON pd.drug_id = d.id  
                    WHERE 
                    ((Date(pack.pickup_date) BETWEEN :startDate AND :endDate)  
                     AND DATE(pack.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) >= :startDate  
                     and DATE(pack.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack.pickup_date as timestamp))/30 as integer))) <= :endDate) 
                   AND pack.clinic_id = :clinicId 
                    GROUP BY pvd.id, d.name
                )                 
                 select   
                     psi.value,  
                     pat.first_names ,  
                     pat.middle_names,  
                     pat.last_names ,  
                 CASE 
                   WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Inicio') 
                        AND (ssr.code = 'NOVO_PACIENTE')
                        AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                    THEN 'Inicio'
                    ELSE
                        CASE
                            WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Transfer de')
                                AND ssr.code = 'TRANSFERIDO_DE'
                                AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                            THEN 'Transfer de'
                            ELSE
                                CASE
                                    WHEN (pr.patient_type = 'N/A' OR pr.patient_type IS null OR pr.patient_type = 'Reiniciar')
                                        AND ssr.code = 'REINICIO_TRATAMETO'
                                        AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                                    THEN 'Reiniciar'
                                    ELSE
                                        CASE
                                            WHEN (ssr.code = 'TRANSITO' OR ssr.code = 'INICIO_MATERNIDADE')
                                                AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '90 days'))
                                            THEN 'Trânsito'
                                            ELSE
                                                CASE
                                                    WHEN ssr.code = 'TERMINO_DO_TRATAMENTO'
                                                        AND (Date(ep.episode_date) <= Date(pack2.pickup_date) AND pack2.pickup_date <= (ep.episode_date + INTERVAL '3 days'))
                                                    THEN 'Termino do Tratamento'
                                                    ELSE
                                                        'Manutenção'
                                                END
                                        END
                                END
                        END
                    END as patient_type,
                     pat.date_of_birth, 
                     CASE   
                      WHEN dt.code = 'DT' THEN 
                        CASE WHEN  pack2.pickup_date >= :startDate THEN 'DT' 
                           ELSE 'DT-TRANSPORTE' 
                             END  
                         WHEN dt.code = 'DS' THEN  
                           CASE WHEN  pack2.pickup_date >= :startDate THEN 'DS'  
                             ELSE 'DS-TRANSPORTE' 
                               END  
                            WHEN dt.code = 'DM' THEN  
                            CASE WHEN  pack2.pickup_date >= :startDate THEN 'DM'  
                              ELSE 'DM-TRANSPORTE' 
                              END 
                         END AS tipodispensa, 
                     tl.description as description2, 
                     pack2.pickup_date, 
                     pack2.next_pick_up_date, 
                     tr.description as description3, 
                     pack2.id as id1, 
                     ssr.reason, 
                     cs.code, 
                     ssr.is_start_reason,
                     pvd.id as id2,
                     r.r_drug_name,
                     r.r_quantity_drugs
                     from patient_visit_details  pvd  
                     inner join ResultTable r on pvd.id = r.r_pvd_id
                     inner join pack pack2 on (pvd.pack_id = pack2.id)  
                     inner join prescription pr on (pvd.prescription_id = pr.id)  
                     inner join prescription_detail prd on (pr.id = prd.prescription_id) 
                     inner join dispense_type dt on (prd.dispense_type_id = dt.id)  
                     inner join episode ep on (ep.id = pvd.episode_id)  
                     inner join start_stop_reason ssr on (ep.start_stop_reason_id = ssr.id)  
                     inner join patient_Service_identifier psi on (ep.patient_Service_identifier_id = psi.id)  
                     inner join patient pat on (psi.patient_id = pat.id)  
                     inner join therapeutic_regimen tr on (prd.therapeutic_regimen_id = tr.id) 
                     inner join therapeutic_line tl on (prd.therapeutic_line_id = tl.id) 
                     inner join clinical_service cs on (psi.service_id = cs.id) 
                     where ((Date(pack2.pickup_date) BETWEEN :startDate AND :endDate)  
                     AND DATE(pack2.pickup_date + (INTERVAL '1 month'* cast (date_part('day',  cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) >= :startDate 
                     and DATE(pack2.pickup_date + (INTERVAL '1 month'*cast (date_part('day', cast (:endDate as timestamp) - cast (pack2.pickup_date as timestamp))/30 as integer))) <= :endDate)  
                     and pack2.clinic_id = :clinicId  
                     and psi.service_id = :clinicalServiceId 
                """

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("clinicId", clinicId)
        query.setParameter("startDate", startDate)
        query.setParameter("endDate", endDate)
        query.setParameter("clinicalServiceId", clinicalServiceId)

        List<Object[]> list = query.list()
        return list
    }


    List<PatientVisitDetails> getLastAllByListPatientId(List<String> patientIds) {

        def patientVisitDetailsList = new ArrayList<PatientVisitDetails>()

        patientIds.each { patientId ->

            def patientServiceIdentifierList = PatientServiceIdentifier.createCriteria().list {
                eq('patient.id', patientId)
            }

            patientServiceIdentifierList.each { patientServiceIdentifier ->

                def last3PatientVisitDetailsFromlast3Prescription = PatientVisitDetails.createCriteria().list {
                    createAlias('patientVisit', 'pv' )
                    createAlias('prescription', 'p')
                    createAlias('episode', 'e')
                    eq('pv.patient.id', patientId)
                    eq('e.patientServiceIdentifier.id',patientServiceIdentifier.id)
                    order('p.prescriptionDate', 'desc')
                    maxResults(3)
                }

                last3PatientVisitDetailsFromlast3Prescription.each { patientVisitDetails ->
                    def patientvisitDetailsByPrescription = PatientVisitDetails.findAllByPrescription(patientVisitDetails.prescription)
                    patientVisitDetailsList.addAll(patientvisitDetailsByPrescription as List<PatientVisitDetails>)

                }
            }
        }


//                  def patientServiceIdentifiers = PatientServiceIdentifier.createCriteria().list {
//                      'in'('patient.id', patientIds) // Get all service identifiers for the provided patient IDs
//                  }
//                  def patientServiceIdentifierIds = patientServiceIdentifiers*.id
//                  def lastPrescriptions = PatientVisitDetails.createCriteria().list {
//                      createAlias('patientVisit', 'pv')
//                      createAlias('prescription', 'p')
//                      createAlias('episode', 'e')
//                      'in'('pv.patient.id', patientIds) // Filter by patient IDs
//                      'in'('e.patientServiceIdentifier.id', patientServiceIdentifierIds) // Filter by patientServiceIdentifiers
//                      projections {
//                          groupProperty('e.patientServiceIdentifier.id') // Group by service identifier
//                          max('p.prescriptionDate')                      // Get the latest prescription date
//                      }
//                  }
//
//                  def prescriptionDatesByServiceId = lastPrescriptions.collectEntries { [it[0], it[1]] }
//                  def patientVisitDetailsList = PatientVisitDetails.createCriteria().list {
//                      createAlias('patientVisit', 'pv')
//                      createAlias('prescription', 'p')
//                      createAlias('episode', 'e')
//                      'in'('pv.patient.id', patientIds) // Filter by patient IDs
//                      'in'('e.patientServiceIdentifier.id', prescriptionDatesByServiceId.keySet()) // Match service identifier
//                      'in'('p.prescriptionDate', prescriptionDatesByServiceId.values()) // Match latest prescription date
//                  }

                  return patientVisitDetailsList
    }

}