package mz.org.fgh.sifmoz.backend.stocklevel

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockLevelSpec extends Specification implements DomainUnitTest<StockLevel> {

     void "test domain constraints"() {
        when:
        StockLevel domain = new StockLevel()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
