package mz.org.fgh.sifmoz.backend.serviceattributetype

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicalServiceAttributeTypeSpec extends Specification implements DomainUnitTest<ClinicalServiceAttributeType> {

     void "test domain constraints"() {
        when:
        ClinicalServiceAttributeType domain = new ClinicalServiceAttributeType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
