package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DistrictSpec extends Specification implements DomainUnitTest<District> {

     void "test domain constraints"() {
        when:
        District domain = new District()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
