package mz.org.fgh.sifmoz.backend.reports.stock

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockReportTempSpec extends Specification implements DomainUnitTest<StockReportTemp> {

     void "test domain constraints"() {
        when:
        StockReportTemp domain = new StockReportTemp()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
