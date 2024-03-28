package mz.org.fgh.sifmoz.backend.nationalClinic

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class NationalClinicSpec extends Specification implements DomainUnitTest<NationalClinic> {

     void "test domain constraints"() {
        when:
        NationalClinic domain = new NationalClinic()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
