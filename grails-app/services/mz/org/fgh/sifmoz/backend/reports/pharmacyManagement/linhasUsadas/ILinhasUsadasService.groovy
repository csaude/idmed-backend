package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.linhasUsadas

import mz.org.fgh.sifmoz.backend.multithread.ReportSearchParams
import mz.org.fgh.sifmoz.backend.reports.common.ReportProcessMonitor
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.AbsentPatientsReport
import mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia.MmiaReport

interface ILinhasUsadasService {
    LinhasUsadasReport get(Serializable id)

    List<LinhasUsadasReport> list(Map args)

    Long count()

    LinhasUsadasReport delete(Serializable id)

    LinhasUsadasReport save(LinhasUsadasReport linhaUsadaReport)

    void processReport(ReportSearchParams searchParams, ReportProcessMonitor processMonitor)

    List<LinhasUsadasReport> getLinhasUsadasReportById(String reportId)
}
