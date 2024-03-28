package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class HealthInformationSystemSpec extends Specification implements DomainUnitTest<HealthInformationSystem> {

     void "test domain constraints"() {
        when:
        HealthInformationSystem domain = new HealthInformationSystem()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
