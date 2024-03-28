package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class SystemConfigsSpec extends Specification implements DomainUnitTest<SystemConfigs> {

     void "test domain constraints"() {
        when:
        SystemConfigs domain = new SystemConfigs()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
