package mz.org.fgh.sifmoz.backend.tansreference

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientTransReferenceSpec extends Specification implements DomainUnitTest<PatientTransReference> {

     void "test domain constraints"() {
        when:
        PatientTransReference domain = new PatientTransReference()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
