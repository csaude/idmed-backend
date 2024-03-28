package mz.org.fgh.sifmoz.backend.reports.pharmacyManagement.mmia

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MmiaRegimenSubReportSpec extends Specification implements DomainUnitTest<MmiaRegimenSubReport> {

     void "test domain constraints"() {
        when:
        MmiaRegimenSubReport domain = new MmiaRegimenSubReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
