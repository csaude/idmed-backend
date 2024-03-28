package mz.org.fgh.sifmoz.backend.protection

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MenuSpec extends Specification implements DomainUnitTest<Menu> {

     void "test domain constraints"() {
        when:
        Menu domain = new Menu()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
