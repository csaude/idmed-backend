package mz.org.fgh.sifmoz.backend.clinic

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicSpec extends Specification implements DomainUnitTest<Clinic> {

     void "test domain constraints"() {
        when:
        Clinic domain = new Clinic()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
