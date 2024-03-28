package mz.org.fgh.sifmoz.backend.protection

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class SecUserRoleSpec extends Specification implements DomainUnitTest<SecUserRole> {

     void "test domain constraints"() {
        when:
        SecUserRole domain = new SecUserRole()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
