package mz.org.fgh.sifmoz.backend.patientAttribute

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PatientAttributeSpec extends Specification implements DomainUnitTest<PatientAttribute> {

     void "test domain constraints"() {
        when:
        PatientAttribute domain = new PatientAttribute()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
