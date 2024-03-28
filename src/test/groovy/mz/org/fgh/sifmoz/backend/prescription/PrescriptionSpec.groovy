package mz.org.fgh.sifmoz.backend.prescription

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PrescriptionSpec extends Specification implements DomainUnitTest<Prescription> {

     void "test domain constraints"() {
        when:
        Prescription domain = new Prescription()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
