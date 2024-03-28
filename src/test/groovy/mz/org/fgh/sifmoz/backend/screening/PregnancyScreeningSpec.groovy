package mz.org.fgh.sifmoz.backend.screening

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PregnancyScreeningSpec extends Specification implements DomainUnitTest<PregnancyScreening> {

     void "test domain constraints"() {
        when:
        PregnancyScreening domain = new PregnancyScreening()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
