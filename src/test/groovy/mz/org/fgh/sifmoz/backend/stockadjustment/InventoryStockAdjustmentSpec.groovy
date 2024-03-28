package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class InventoryStockAdjustmentSpec extends Specification implements DomainUnitTest<InventoryStockAdjustment> {

     void "test domain constraints"() {
        when:
        InventoryStockAdjustment domain = new InventoryStockAdjustment()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
