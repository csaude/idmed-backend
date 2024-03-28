package mz.org.fgh.sifmoz.backend.protection

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class RequestmapSpec extends Specification implements DomainUnitTest<Requestmap> {

     void "test domain constraints"() {
        when:
        Requestmap domain = new Requestmap()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
