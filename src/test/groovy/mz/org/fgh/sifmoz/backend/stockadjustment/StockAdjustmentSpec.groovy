package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockAdjustmentSpec extends Specification implements DomainUnitTest<StockAdjustment> {

     void "test domain constraints"() {
        when:
        StockAdjustment domain = new StockAdjustment()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
