package mz.org.fgh.sifmoz.backend.auditTrail

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class AuditTrailSpec extends Specification implements DomainUnitTest<AuditTrail> {

     void "test domain constraints"() {
        when:
        AuditTrail domain = new AuditTrail()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
