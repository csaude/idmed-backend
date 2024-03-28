package mz.org.fgh.sifmoz.backend.facilityType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class FacilityTypeSpec extends Specification implements DomainUnitTest<FacilityType> {

     void "test domain constraints"() {
        when:
        FacilityType domain = new FacilityType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
