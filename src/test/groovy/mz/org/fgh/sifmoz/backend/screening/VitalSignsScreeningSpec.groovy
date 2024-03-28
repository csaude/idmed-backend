package mz.org.fgh.sifmoz.backend.screening

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class VitalSignsScreeningSpec extends Specification implements DomainUnitTest<VitalSignsScreening> {

     void "test domain constraints"() {
        when:
        VitalSignsScreening domain = new VitalSignsScreening()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
