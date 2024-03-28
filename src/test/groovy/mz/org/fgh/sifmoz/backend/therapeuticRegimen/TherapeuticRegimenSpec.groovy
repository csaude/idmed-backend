package mz.org.fgh.sifmoz.backend.therapeuticRegimen

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class TherapeuticRegimenSpec extends Specification implements DomainUnitTest<TherapeuticRegimen> {

     void "test domain constraints"() {
        when:
        TherapeuticRegimen domain = new TherapeuticRegimen()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
