package mz.org.fgh.sifmoz.backend.identifierType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class IdentifierTypeSpec extends Specification implements DomainUnitTest<IdentifierType> {

     void "test domain constraints"() {
        when:
        IdentifierType domain = new IdentifierType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
