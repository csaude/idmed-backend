package mz.org.fgh.sifmoz.backend.stockdestruction

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DestroyedStockSpec extends Specification implements DomainUnitTest<DestroyedStock> {

     void "test domain constraints"() {
        when:
        DestroyedStock domain = new DestroyedStock()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
