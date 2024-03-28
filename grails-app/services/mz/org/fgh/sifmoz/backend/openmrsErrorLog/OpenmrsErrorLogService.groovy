package mz.org.fgh.sifmoz.backend.openmrsErrorLog

import grails.gorm.services.Service

@Service(OpenmrsErrorLog)
interface OpenmrsErrorLogService {

    OpenmrsErrorLog get(Serializable id)

    List<OpenmrsErrorLog> list(Map args)

    Long count()

    OpenmrsErrorLog delete(Serializable id)

    OpenmrsErrorLog save(OpenmrsErrorLog openmrsErrorLog)

}
