package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.historicoLevantamento

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class HistoricoLevantamentoReportSpec extends Specification implements DomainUnitTest<HistoricoLevantamentoReport> {

     void "test domain constraints"() {
        when:
        HistoricoLevantamentoReport domain = new HistoricoLevantamentoReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
