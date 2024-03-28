package mz.org.fgh.sifmoz.backend.packaging

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PackSpec extends Specification implements DomainUnitTest<Pack> {

     void "test domain constraints"() {
        when:
        Pack domain = new Pack()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
