package mz.org.fgh.sifmoz.backend.dispenseType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DispenseTypeSpec extends Specification implements DomainUnitTest<DispenseType> {

     void "test domain constraints"() {
        when:
        DispenseType domain = new DispenseType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
