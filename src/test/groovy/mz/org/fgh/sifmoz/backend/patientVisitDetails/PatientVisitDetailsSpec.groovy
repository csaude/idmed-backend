package mz.org.fgh.sifmoz.backend.patientVisitDetails

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientVisitDetailsSpec extends Specification implements DomainUnitTest<PatientVisitDetails> {

     void "test domain constraints"() {
        when:
        PatientVisitDetails domain = new PatientVisitDetails()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
