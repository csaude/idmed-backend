package mz.org.fgh.sifmoz.backend.prescriptionDetail

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class PrescriptionDetailSpec extends Specification implements DomainUnitTest<PrescriptionDetail> {

     void "test domain constraints"() {
        when:
        PrescriptionDetail domain = new PrescriptionDetail()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
