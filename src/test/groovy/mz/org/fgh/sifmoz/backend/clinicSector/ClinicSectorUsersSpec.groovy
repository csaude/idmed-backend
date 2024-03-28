package mz.org.fgh.sifmoz.backend.clinicSector

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicSectorUsersSpec extends Specification implements DomainUnitTest<ClinicSectorUsers> {

     void "test domain constraints"() {
        when:
        ClinicSectorUsers domain = new ClinicSectorUsers()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
