package mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PostoAdministrativoSpec extends Specification implements DomainUnitTest<PostoAdministrativo> {

     void "test domain constraints"() {
        when:
        PostoAdministrativo domain = new PostoAdministrativo()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
