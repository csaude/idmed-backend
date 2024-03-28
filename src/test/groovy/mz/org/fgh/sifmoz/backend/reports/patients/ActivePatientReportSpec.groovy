package mz.org.fgh.sifmoz.backend.reports.patients

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ActivePatientReportSpec extends Specification implements DomainUnitTest<ActivePatientReport> {

     void "test domain constraints"() {
        when:
        ActivePatientReport domain = new ActivePatientReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
