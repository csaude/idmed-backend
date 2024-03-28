package mz.org.fgh.sifmoz.backend.interoperabilityAttribute

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class InteroperabilityAttributeSpec extends Specification implements DomainUnitTest<InteroperabilityAttribute> {

     void "test domain constraints"() {
        when:
        InteroperabilityAttribute domain = new InteroperabilityAttribute()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
