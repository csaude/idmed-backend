package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class LocalidadeSpec extends Specification implements DomainUnitTest<Localidade> {

     void "test domain constraints"() {
        when:
        Localidade domain = new Localidade()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
