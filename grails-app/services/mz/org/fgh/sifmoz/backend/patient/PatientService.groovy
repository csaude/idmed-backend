package mz.org.fgh.sifmoz.backend.patient

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(Patient)
abstract class PatientService implements IPatientService {

    @Autowired
    SessionFactory sessionFactory

    @Override
    List<Patient> search(Patient patient) {
        //Check wether identifier exists
        boolean hasIdentifier = Utilities.listHasElements(patient.identifiers as ArrayList<?>)
        String mainQuery = "select p from Patient p " +
                " where (lower(p.firstNames) like lower(:firstNames) OR" +
                " lower(p.middleNames) like lower(:middleNames) OR " +
                " lower(p.lastNames) like lower(:lastNames)) " +
                " AND p.clinic =:clinic"
        String indentifierCondition = " OR EXISTS (select psi " +
                "                   from PatientServiceIdentifier psi inner join psi.patient pt " +
                "                   where pt.id = p.id and lower(psi.value) like lower(:identifiers)) "
        String searchQuery = mainQuery + (hasIdentifier ? indentifierCondition : "")

        searchQuery += " order by p.firstNames "

        Clinic clinic = Clinic.findById(patient.clinic.id)

        if (hasIdentifier)

            return Patient.executeQuery(searchQuery,
                    [firstNames : "%${patient.firstNames}%",
                     middleNames: "%${patient.middleNames}%",
                     lastNames  : "%${patient.lastNames}%",
                     clinic     : clinic,
                     identifiers: (Utilities.listHasElements(patient.identifiers as ArrayList<?>) ? "%${patient.identifiers.getAt(0).value}%" : ""), max: 400]
            )
        else
            return Patient.executeQuery(searchQuery,
                    [firstNames : "%${patient.firstNames}%",
                     middleNames: "%${patient.middleNames}%",
                     lastNames  : "%${patient.lastNames}%",
                     clinic     : clinic]
            )
        //return Patient.findAllByFirstNamesIlikeOrMiddleNamesIlikeOrLastNamesIlike("%${patient.firstNames}%", "%${patient.middleNames}%", "%${patient.lastNames}%")
    }

    @Override
    List<Patient> search(String searchString, String clinicId) {
        String mainQuery = "select p from Patient p " +
                " where (lower(p.firstNames) like lower(:searchString) OR" +
                " lower(p.middleNames) like lower(:searchString) OR " +
                " lower(p.lastNames) like lower(:searchString)) " +
                " AND p.clinic =:clinic"
        String indentifierCondition = " OR EXISTS (select psi " +
                "                   from PatientServiceIdentifier psi inner join psi.patient pt " +
                "                   where pt.id = p.id and lower(psi.value) like lower(:searchString)) "
        String searchQuery = mainQuery + indentifierCondition

        searchQuery += " order by p.firstNames "

        Clinic clinic = Clinic.findById(clinicId)

        return Patient.executeQuery(searchQuery,
                [searchString: "%${searchString}%",
                 clinic      : clinic, max: 400]
        )
    }

    @Override
    Long count(Patient patient) {
        return Patient.countByFirstNamesIlikeOrMiddleNamesIlikeOrLastNamesIlike("%${patient.firstNames}%", "%${patient.middleNames}%", "%${patient.lastNames}%")
    }

    @Override
    List<Patient> getAllByClinicId(String clinicId, int offset, int max) {
        return Patient.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

    @Override
    List<Patient> getAllPatientsInClinicSector(ClinicSector clinicSector) {

        def patients = Patient.executeQuery("select p from Episode ep " +
                "inner join ep.startStopReason stp " +
                "inner join ep.patientServiceIdentifier psi " +
                "inner join psi.patient p " +
                "inner join ep.clinic c " +
                "where ep.clinicSector = :clinicSector " +
                "and exists (select pvd from PatientVisitDetails pvd where pvd.episode = ep ) " +
                "and ep.episodeDate = ( " +
                "  SELECT MAX(e.episodeDate)" +
                "  FROM Episode e" +
                " inner join e.patientServiceIdentifier psi2" +
                "  WHERE psi2 = ep.patientServiceIdentifier and e.clinicSector = :clinicSector" +
                ")" +
                "order by ep.episodeDate desc", [clinicSector: clinicSector])

        patients.each { p ->
            p.identifiers = []
            //  p.patientVisits = []
        }
        return patients
    }

    @Override
    List getPatientWithoutDispense(ReportSearchParams reportSearchParams) {

        def queryString = "select pat.first_names , pat.middle_names , pat.last_names ," +
                "  psi.value ,pat.his_uuid, pat.creation_date  from patient pat " +
                " inner join patient_service_identifier psi   on (pat.id = psi.patient_id) " +
                " where pat.id  not in (select pv.patient_id from patient_visit pv  )" +
                " AND psi.clinic_id =:clinic_id" +
                " and pat.creation_date >=:startDate and pat.creation_date <=:endDate"

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("endDate", reportSearchParams.endDate)
        query.setParameter("startDate", reportSearchParams.startDate)
        query.setParameter("clinic_id", reportSearchParams.clinicId)
        List<Object[]> list = query.list()
        return list
    }

    @Override
    List getAllExpectedPatients(ReportSearchParams reportSearchParams) {
        def queryString = " select distinct ON (p.id) " +
                "                p.first_names,  " +
                "                    p.middle_names,   " +
                "                    p.last_names,  " +
                "                    p.cellphone, " +
                "                    r3.nextPickUpDate," +
                "                    r1.t_regimen, " +
                "                     r1.dt_description, " +
                "                     r3.clinicName, " +
                "                    r.value " +
                "                from patient p  " +
                "                inner join patient_visit pv on pv.patient_id = p.id  " +
                "                inner join patient_visit_details pvd on pvd.patient_visit_id = pv.id  " +
                "                inner join (  " +
                "                select  " +
                "                psi.patient_id as pat_id,  " +
                "                psi.value  " +
                "                from patient_service_identifier psi   " +
                "                inner join clinical_service cs on psi.service_id = cs.id and cs.code = 'TARV'  " +
                "                ) r on r.pat_id = p.id  " +
                "                inner join (  " +
                "                select  " +
                "                pre.id,  " +
                "                tl.description as t_line,  " +
                "                tr.description as t_regimen,  " +
                "                pre.patient_type, " +
                "                dt.code as dtcode, "+
                   "             dt.description as dt_description " +
                "                from prescription pre  " +
                "                inner join prescription_detail pd on pd.prescription_id = pre.id  " +
                "                inner join therapeutic_line tl on pd.therapeutic_line_id = tl.id  " +
                "                inner join therapeutic_regimen tr on pd.therapeutic_regimen_id = tr.id   " +
                "                inner join dispense_type dt ON dt.id = pd.dispense_type_id " +
                "                ) r1 on pvd.prescription_id = r1.id  " +
                "                inner join (  " +
                "                select   " +
                "                pk.id as pack_id,  " +
                "                pk.next_pick_up_date as nextPickUpDate, " +
                "                MAX(pk.pickup_date) AS pickUpDate," +
                "  c.clinic_name as clinicName " +
                "                from pack pk  " +
                "                inner join clinic c on pk.clinic_id = c.id  and c.id = :clinic_id  " +
                "                group by pk.id, pk.next_pick_up_date,  c.clinic_name " +
                "                ) r3 on pvd.pack_id = r3.pack_id  " +
                "                where   r3.nextPickUpDate >=:startDate and   r3.nextPickUpDate <=:endDate "
        //and   r3.nextPickUpDate >=current_timestamp

        Session session = sessionFactory.getCurrentSession()
        def query = session.createSQLQuery(queryString)
        query.setParameter("endDate", reportSearchParams.endDate)
        query.setParameter("startDate", reportSearchParams.startDate)
        query.setParameter("clinic_id", reportSearchParams.clinicId)
        List<Object[]> list = query.list()
        return list
    }

}
