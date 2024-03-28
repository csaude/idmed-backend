package mz.org.fgh.sifmoz.backend.patient

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientSpec extends Specification implements DomainUnitTest<Patient> {

     void "test domain constraints"() {
        when:
        Patient domain = new Patient()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
