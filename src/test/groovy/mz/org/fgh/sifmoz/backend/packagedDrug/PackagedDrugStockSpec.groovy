package mz.org.fgh.sifmoz.backend.packagedDrug

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PackagedDrugStockSpec extends Specification implements DomainUnitTest<PackagedDrugStock> {

     void "test domain constraints"() {
        when:
        PackagedDrugStock domain = new PackagedDrugStock()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
