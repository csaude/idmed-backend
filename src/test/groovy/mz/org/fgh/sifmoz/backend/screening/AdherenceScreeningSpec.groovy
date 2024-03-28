package mz.org.fgh.sifmoz.backend.screening

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AdherenceScreeningSpec extends Specification implements DomainUnitTest<AdherenceScreening> {

     void "test domain constraints"() {
        when:
        AdherenceScreening domain = new AdherenceScreening()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
