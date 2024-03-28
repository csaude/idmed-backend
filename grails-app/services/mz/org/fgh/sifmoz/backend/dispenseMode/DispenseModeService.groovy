package mz.org.fgh.sifmoz.backend.dispenseMode

import grails.gorm.services.Service

@Service(DispenseMode)
interface DispenseModeService {

    DispenseMode get(Serializable id)

    List<DispenseMode> list(Map args)

    Long count()

    DispenseMode delete(Serializable id)

    DispenseMode save(DispenseMode dispenseMode)

}
