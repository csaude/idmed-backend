package mz.org.fgh.sifmoz.backend.reports.stock

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class UsedStockReportTempSpec extends Specification implements DomainUnitTest<UsedStockReportTemp> {

     void "test domain constraints"() {
        when:
        UsedStockReportTemp domain = new UsedStockReportTemp()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
