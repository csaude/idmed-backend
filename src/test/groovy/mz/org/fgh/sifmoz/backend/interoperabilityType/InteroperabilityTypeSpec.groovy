package mz.org.fgh.sifmoz.backend.interoperabilityType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class InteroperabilityTypeSpec extends Specification implements DomainUnitTest<InteroperabilityType> {

     void "test domain constraints"() {
        when:
        InteroperabilityType domain = new InteroperabilityType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
