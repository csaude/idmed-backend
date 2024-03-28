package mz.org.fgh.sifmoz.backend.provincialServer

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ProvincialServerSpec extends Specification implements DomainUnitTest<ProvincialServer> {

     void "test domain constraints"() {
        when:
        ProvincialServer domain = new ProvincialServer()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
