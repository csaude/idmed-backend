package mz.org.fgh.sifmoz.backend.drug

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DrugSpec extends Specification implements DomainUnitTest<Drug> {

     void "test domain constraints"() {
        when:
        Drug domain = new Drug()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
