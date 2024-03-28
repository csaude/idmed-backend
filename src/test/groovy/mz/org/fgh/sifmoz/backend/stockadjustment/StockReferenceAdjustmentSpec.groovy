package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockReferenceAdjustmentSpec extends Specification implements DomainUnitTest<StockReferenceAdjustment> {

     void "test domain constraints"() {
        when:
        StockReferenceAdjustment domain = new StockReferenceAdjustment()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
