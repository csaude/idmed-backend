package mz.org.fgh.sifmoz.backend.clinicSector

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicSectorSpec extends Specification implements DomainUnitTest<ClinicSector> {

     void "test domain constraints"() {
        when:
        ClinicSector domain = new ClinicSector()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
