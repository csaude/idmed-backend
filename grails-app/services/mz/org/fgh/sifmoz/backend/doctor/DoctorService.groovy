package mz.org.fgh.sifmoz.backend.doctor

import grails.gorm.services.Service

@Service(Doctor)
interface DoctorService {

    Doctor get(Serializable id)

    List<Doctor> list(Map args)

    Long count()

    Doctor delete(Serializable id)

    Doctor save(Doctor doctor)

}
