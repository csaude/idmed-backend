package mz.org.fgh.sifmoz.backend.serviceProgram

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ServicePatientSpec extends Specification implements DomainUnitTest<ServicePatient> {

     void "test domain constraints"() {
        when:
        ServicePatient domain = new ServicePatient()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
