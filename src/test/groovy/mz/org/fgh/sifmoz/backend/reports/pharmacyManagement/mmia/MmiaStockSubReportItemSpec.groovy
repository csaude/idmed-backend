package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MmiaStockSubReportItemSpec extends Specification implements DomainUnitTest<MmiaStockSubReportItem> {

     void "test domain constraints"() {
        when:
        MmiaStockSubReportItem domain = new MmiaStockSubReportItem()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
