package mz.org.fgh.sifmoz.backend.migrationLog

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MigrationLogSpec extends Specification implements DomainUnitTest<MigrationLog> {

     void "test domain constraints"() {
        when:
        MigrationLog domain = new MigrationLog()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
