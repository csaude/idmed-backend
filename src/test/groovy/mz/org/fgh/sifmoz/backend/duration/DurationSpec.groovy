package mz.org.fgh.sifmoz.backend.duration

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DurationSpec extends Specification implements DomainUnitTest<Duration> {

     void "test domain constraints"() {
        when:
        Duration domain = new Duration()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
