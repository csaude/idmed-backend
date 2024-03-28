package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AbsentPatientsReportSpec extends Specification implements DomainUnitTest<AbsentPatientsReport> {

     void "test domain constraints"() {
        when:
        AbsentPatientsReport domain = new AbsentPatientsReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
