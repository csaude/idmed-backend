package mz.org.fgh.sifmoz.backend.patientUpdateOpenMrsErrorLog

import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit


interface IPatientUpdateOpenMrsErrorLogControllerService {

    PatientUpdateOpenMrsErrorLog get(Serializable id)

    List<PatientUpdateOpenMrsErrorLog> list(Map args)

    Long count()

    PatientUpdateOpenMrsErrorLog delete(Serializable id)

    PatientUpdateOpenMrsErrorLog save(PatientUpdateOpenMrsErrorLog visit)


}
