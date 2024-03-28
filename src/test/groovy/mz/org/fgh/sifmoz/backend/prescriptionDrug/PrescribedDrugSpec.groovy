package mz.org.fgh.sifmoz.backend.prescriptionDrug

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PrescribedDrugSpec extends Specification implements DomainUnitTest<PrescribedDrug> {

     void "test domain constraints"() {
        when:
        PrescribedDrug domain = new PrescribedDrug()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
