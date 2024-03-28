package mz.org.fgh.sifmoz.backend.startStopReason

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StartStopReasonSpec extends Specification implements DomainUnitTest<StartStopReason> {

     void "test domain constraints"() {
        when:
        StartStopReason domain = new StartStopReason()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
