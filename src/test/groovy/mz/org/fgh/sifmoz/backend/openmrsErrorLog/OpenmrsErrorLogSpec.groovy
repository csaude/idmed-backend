package mz.org.fgh.sifmoz.backend.openmrsErrorLog

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class OpenmrsErrorLogSpec extends Specification implements DomainUnitTest<OpenmrsErrorLog> {

     void "test domain constraints"() {
        when:
        OpenmrsErrorLog domain = new OpenmrsErrorLog()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
