package mz.org.fgh.sifmoz.backend.patientIdentifier

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientServiceIdentifierSpec extends Specification implements DomainUnitTest<PatientServiceIdentifier> {

     void "test domain constraints"() {
        when:
        PatientServiceIdentifier domain = new PatientServiceIdentifier()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
