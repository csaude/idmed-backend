package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockDestructionAdjustmentSpec extends Specification implements DomainUnitTest<StockDestructionAdjustment> {

     void "test domain constraints"() {
        when:
        StockDestructionAdjustment domain = new StockDestructionAdjustment()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
