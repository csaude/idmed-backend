package mz.org.fgh.sifmoz.backend.protection

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class SecUserSpec extends Specification implements DomainUnitTest<SecUser> {

     void "test domain constraints"() {
        when:
        SecUser domain = new SecUser()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
