package mz.org.fgh.sifmoz.backend.prescription

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class SpetialPrescriptionMotiveSpec extends Specification implements DomainUnitTest<SpetialPrescriptionMotive> {

     void "test domain constraints"() {
        when:
        SpetialPrescriptionMotive domain = new SpetialPrescriptionMotive()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
