package mz.org.fgh.sifmoz.backend.appointment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AppointmetSpec extends Specification implements DomainUnitTest<Appointmet> {

     void "test domain constraints"() {
        when:
        Appointmet domain = new Appointmet()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
