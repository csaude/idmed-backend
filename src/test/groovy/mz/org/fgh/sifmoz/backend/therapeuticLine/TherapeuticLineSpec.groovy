package mz.org.fgh.sifmoz.backend.therapeuticLine

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class TherapeuticLineSpec extends Specification implements DomainUnitTest<TherapeuticLine> {

     void "test domain constraints"() {
        when:
        TherapeuticLine domain = new TherapeuticLine()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
