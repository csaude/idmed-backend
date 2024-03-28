package mz.org.fgh.sifmoz.backend.reports.referralManagement

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ReferredPatientsReportSpec extends Specification implements DomainUnitTest<ReferredPatientsReport> {

     void "test domain constraints"() {
        when:
        ReferredPatientsReport domain = new ReferredPatientsReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
