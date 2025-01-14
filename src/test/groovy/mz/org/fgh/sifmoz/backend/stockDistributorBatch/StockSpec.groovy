package mz.org.fgh.sifmoz.backend.stockDistributorBatch

import grails.testing.gorm.DomainUnitTest
import mz.org.fgh.sifmoz.backend.stock.Stock
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
