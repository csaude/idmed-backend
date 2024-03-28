package mz.org.fgh.sifmoz.backend.stockrefered

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ReferedStockMovimentSpec extends Specification implements DomainUnitTest<ReferedStockMoviment> {

     void "test domain constraints"() {
        when:
        ReferedStockMoviment domain = new ReferedStockMoviment()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
