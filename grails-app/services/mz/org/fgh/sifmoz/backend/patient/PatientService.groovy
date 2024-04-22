package mz.org.fgh.sifmoz.backend.patient

import grails.converters.JSON
import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.hibernate.Session
import org.hibernate.SessionFactory

@Transactional
@Service(Patient)
abstract class PatientService implements IPatientService{

    SessionFactory sessionFactory

    @Override
    List<Patient> search(Patient patient,int offset, int limit) {
        def queryString = "select p from Patient p where 1 = 1"
        Map<String, Object> parameters = [:]

        if (patient.firstNames && patient.lastNames) {
            queryString += """
        and (
            lower(unaccent(p.firstNames)) like lower(unaccent(:firstNames))
            and lower(unaccent(p.lastNames)) like lower(unaccent(:lastNames))
        )
    """
            parameters["firstNames"] = "%" + patient.firstNames + "%"
            parameters["lastNames"] = "%" + patient.lastNames + "%"
        } else if (patient.firstNames) {
            queryString += """
        and (
            lower(unaccent(p.firstNames)) like lower(unaccent(:name))
        )
    """
            parameters["name"] = "%" + patient.firstNames + "%"
        } else if (patient.lastNames) {
            queryString += """
        and (
            lower(unaccent(p.firstNames)) like lower(unaccent(:name))
        )
    """
            parameters["name"] = "%" + patient.lastNames + "%"
        }
        if (patient.identifiers.first().value != null) {
            queryString += " and p.id in (select distinct(psi.patient.id) from PatientServiceIdentifier psi where psi.value like :identifierValue)"
            parameters["identifierValue"] = "%" + patient.identifiers.first().value.trim() + "%"
        }
        Session session = sessionFactory.currentSession
        def query = session.createQuery(queryString)

        parameters.each { key, value ->
            query.setParameter(key, value)
        }

        query.setFirstResult(offset)
        query.setMaxResults(limit)
        List<Patient> patientList = query.list()

        return patientList
        def result = JSONSerializer.setLightObjectListJsonResponse(patientList)
        (result as List).collect { rs ->
            def auxPatient = Patient.get(rs.id)
            if (auxPatient.identifiers.size() > 0)
                rs.put('identifiers', auxPatient.identifiers)
        }


      return result
    }

    @Override
    List<Patient> search(String searchString, String clinicId) {
        String mainQuery =  "select p from Patient p " +
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
                 clinic: clinic, max: 400]
        )
    }

    @Override
    Long count(Patient patient) {
        return Patient.countByFirstNamesIlikeOrMiddleNamesIlikeOrLastNamesIlike("%${patient.firstNames}%", "%${patient.middleNames}%", "%${patient.lastNames}%")
    }

    @Override
    List<Patient> getAllByClinicId(String clinicId, int offset, int max) {
        return Patient.findAllByClinic(Clinic.findById(clinicId),[offset: offset, max: max])
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

        patients.each{ p ->
            p.identifiers = []
          //  p.patientVisits = []
        }
        return patients
    }

    @Override
    Long countPatientSearchResult(Patient patient) {
        def queryString = "select count(p.id) from Patient p where 1 = 1"
        Map<String, Object> parameters = [:]

        if (patient.firstNames && patient.lastNames) {
            queryString += """
        and (
            lower(unaccent(p.firstNames)) like lower(unaccent(:firstNames))
            and lower(unaccent(p.lastNames)) like lower(unaccent(:lastNames))
        )
    """
            parameters["firstNames"] = "%" + patient.firstNames + "%"
            parameters["lastNames"] = "%" + patient.lastNames + "%"
        } else if (patient.firstNames) {
            queryString += """
        and (
            lower(unaccent(p.firstNames)) like lower(unaccent(:name))
        )
    """
            parameters["name"] = "%" + patient.firstNames + "%"
        } else if (patient.lastNames) {
            queryString += """
        and (
             lower(unaccent(p.lastNames)) like lower(unaccent(:name))
        )
    """
            parameters["name"] = "%" + patient.lastNames + "%"
        }
        if (patient.identifiers.first().value != null) {
            queryString += " and p.id in (select distinct(psi.patient.id) from PatientServiceIdentifier psi where psi.value like :identifierValue)"
            parameters["identifierValue"] = "%" + patient.identifiers.first().value.trim() + "%"
        }

        Session session = sessionFactory.currentSession
        def query = session.createQuery(queryString)

        parameters.each { key, value ->
            query.setParameter(key, value)
        }
        Long totalCount = query.uniqueResult()


        return totalCount
    }

}
