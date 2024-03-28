package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ProvinceSpec extends Specification implements DomainUnitTest<Province> {

     void "test domain constraints"() {
        when:
        Province domain = new Province()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
