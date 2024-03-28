package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.gorm.services.Service

@Service(MmiaRegimenSubReport)
interface MmiaRegimenSubReportService {

    MmiaRegimenSubReport get(Serializable id)

    List<MmiaRegimenSubReport> list(Map args)

    Long count()

    MmiaRegimenSubReport delete(Serializable id)

    MmiaRegimenSubReport save(MmiaRegimenSubReport mmiaRegimenSubReport)
}
