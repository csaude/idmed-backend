package mz.org.fgh.sifmoz.backend.reports.common

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ReportProcessMonitorSpec extends Specification implements DomainUnitTest<ReportProcessMonitor> {

     void "test domain constraints"() {
        when:
        ReportProcessMonitor domain = new ReportProcessMonitor()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
