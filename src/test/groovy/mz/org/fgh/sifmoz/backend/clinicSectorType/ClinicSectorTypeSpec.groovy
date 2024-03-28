package mz.org.fgh.sifmoz.backend.clinicSectorType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ClinicSectorTypeSpec extends Specification implements DomainUnitTest<ClinicSectorType> {

     void "test domain constraints"() {
        when:
        ClinicSectorType domain = new ClinicSectorType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
