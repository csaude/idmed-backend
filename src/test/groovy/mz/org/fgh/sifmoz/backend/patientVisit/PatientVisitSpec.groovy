package mz.org.fgh.sifmoz.backend.patientVisit

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientVisitSpec extends Specification implements DomainUnitTest<PatientVisit> {

     void "test domain constraints"() {
        when:
        PatientVisit domain = new PatientVisit()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
