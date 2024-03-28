package mz.org.fgh.sifmoz.backend.tansreference

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientTransReferenceTypeSpec extends Specification implements DomainUnitTest<PatientTransReferenceType> {

     void "test domain constraints"() {
        when:
        PatientTransReferenceType domain = new PatientTransReferenceType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
