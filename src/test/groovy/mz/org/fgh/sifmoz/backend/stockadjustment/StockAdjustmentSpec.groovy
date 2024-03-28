package mz.org.fgh.sifmoz.backend.stockadjustment

import grails.testing.gorm.DomainUnitTest
import mz.org.fgh.sifmoz.backend.protection.Menu
import spock.lang.Specification

class StockAdjustmentSpec extends Specification implements DomainUnitTest<StockAdjustment> {

     void "test domain constraints"() {
        when:
        StockAdjustment domain = new StockAdjustment() {
            @Override
            List<Menu> hasMenus() {
                return null
            }
        }
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
