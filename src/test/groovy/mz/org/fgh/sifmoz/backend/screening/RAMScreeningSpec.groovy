package mz.org.fgh.sifmoz.backend.screening

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class RAMScreeningSpec extends Specification implements DomainUnitTest<RAMScreening> {

     void "test domain constraints"() {
        when:
        RAMScreening domain = new RAMScreening()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
