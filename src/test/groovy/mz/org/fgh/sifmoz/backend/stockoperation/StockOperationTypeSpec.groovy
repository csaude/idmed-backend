package mz.org.fgh.sifmoz.backend.stockoperation

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class StockOperationTypeSpec extends Specification implements DomainUnitTest<StockOperationType> {

     void "test domain constraints"() {
        when:
        StockOperationType domain = new StockOperationType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
