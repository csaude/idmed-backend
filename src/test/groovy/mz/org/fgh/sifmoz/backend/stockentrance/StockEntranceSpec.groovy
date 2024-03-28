package mz.org.fgh.sifmoz.backend.stockentrance

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockEntranceSpec extends Specification implements DomainUnitTest<StockEntrance> {

     void "test domain constraints"() {
        when:
        StockEntrance domain = new StockEntrance()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
