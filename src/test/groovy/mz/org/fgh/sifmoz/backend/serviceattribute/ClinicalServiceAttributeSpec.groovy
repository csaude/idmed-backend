package mz.org.fgh.sifmoz.backend.serviceattribute

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicalServiceAttributeSpec extends Specification implements DomainUnitTest<ClinicalServiceAttribute> {

     void "test domain constraints"() {
        when:
        ClinicalServiceAttribute domain = new ClinicalServiceAttribute()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
