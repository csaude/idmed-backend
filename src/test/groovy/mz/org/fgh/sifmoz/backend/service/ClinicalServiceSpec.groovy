package mz.org.fgh.sifmoz.backend.service

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicalServiceSpec extends Specification implements DomainUnitTest<ClinicalService> {

     void "test domain constraints"() {
        when:
        ClinicalService domain = new ClinicalService()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
