package mz.org.fgh.sifmoz.backend.screening

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class TBScreeningSpec extends Specification implements DomainUnitTest<TBScreening> {

     void "test domain constraints"() {
        when:
        TBScreening domain = new TBScreening()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
