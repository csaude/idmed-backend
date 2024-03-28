package mz.org.fgh.sifmoz.backend.packagedDrug

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PackagedDrugSpec extends Specification implements DomainUnitTest<PackagedDrug> {

     void "test domain constraints"() {
        when:
        PackagedDrug domain = new PackagedDrug()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
