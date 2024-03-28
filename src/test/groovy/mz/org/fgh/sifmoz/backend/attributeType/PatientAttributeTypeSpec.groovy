package mz.org.fgh.sifmoz.backend.attributeType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientAttributeTypeSpec extends Specification implements DomainUnitTest<PatientAttributeType> {

     void "test domain constraints"() {
        when:
        PatientAttributeType domain = new PatientAttributeType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
