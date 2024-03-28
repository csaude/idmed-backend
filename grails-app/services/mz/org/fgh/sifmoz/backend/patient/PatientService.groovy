package mz.org.fgh.sifmoz.backend.patient

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.utilities.Utilities

@Transactional
@Service(Patient)
abstract class PatientService implements IPatientService{

    @Override
    List<Patient> search(Patient patient) {
        //Check wether identifier exists
        boolean hasIdentifier = Utilities.listHasElements(patient.identifiers as ArrayList<?>)
        String mainQuery =  "select p from Patient p " +
                            " where (lower(p.firstNames) like lower(:firstNames) OR" +
                            " lower(p.middleNames) like lower(:middleNames) OR " +
                            " lower(p.lastNames) like lower(:lastNames)) " +
                            " AND p.clinic =:clinic"
        String indentifierCondition = " OR EXISTS (select psi " +
                                "                   from PatientServiceIdentifier psi inner join psi.patient pt " +
                                "                   where pt.id = p.id and lower(psi.value) like lower(:identifiers)) "
        String searchQuery = mainQuery + (hasIdentifier? indentifierCondition : "")

                searchQuery += " order by p.firstNames "

        Clinic clinic = Clinic.findById(patient.clinic.id)

        if(hasIdentifier)

        return Patient.executeQuery(searchQuery,
                                    [firstNames: "%${patient.firstNames}%",
                                     middleNames: "%${patient.middleNames}%",
                                     lastNames: "%${patient.lastNames}%",
                                     clinic: clinic,
                                     identifiers: (Utilities.listHasElements(patient.identifiers as ArrayList<?>) ? "%${patient.identifiers.getAt(0).value}%" : ""), max: 400]
                                    )
        else
            return Patient.executeQuery(searchQuery,
                    [firstNames: "%${patient.firstNames}%",
                     middleNames: "%${patient.middleNames}%",
                     lastNames: "%${patient.lastNames}%",
                     clinic: clinic]
            )
        //return Patient.findAllByFirstNamesIlikeOrMiddleNamesIlikeOrLastNamesIlike("%${patient.firstNames}%", "%${patient.middleNames}%", "%${patient.lastNames}%")
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

}
