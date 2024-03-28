package mz.org.fgh.sifmoz.backend.stockcenter

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockCenterSpec extends Specification implements DomainUnitTest<StockCenter> {

     void "test domain constraints"() {
        when:
        StockCenter domain = new StockCenter()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
