package mz.org.fgh.sifmoz.backend.form

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class FormSpec extends Specification implements DomainUnitTest<Form> {

     void "test domain constraints"() {
        when:
        Form domain = new Form()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
