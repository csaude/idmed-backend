package mz.org.fgh.sifmoz.backend.stock

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockSpec extends Specification implements DomainUnitTest<Stock> {

     void "test domain constraints"() {
        when:
        Stock domain = new Stock()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
