package mz.org.fgh.sifmoz.backend.dispenseMode

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DispenseModeSpec extends Specification implements DomainUnitTest<DispenseMode> {

     void "test domain constraints"() {
        when:
        DispenseMode domain = new DispenseMode()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
