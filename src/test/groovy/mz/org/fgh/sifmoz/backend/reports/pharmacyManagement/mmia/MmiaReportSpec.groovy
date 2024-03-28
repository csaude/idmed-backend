package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MmiaReportSpec extends Specification implements DomainUnitTest<MmiaReport> {

     void "test domain constraints"() {
        when:
        MmiaReport domain = new MmiaReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
