package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class DrugQuantityTempSpec extends Specification implements DomainUnitTest<DrugQuantityTemp> {

     void "test domain constraints"() {
        when:
        DrugQuantityTemp domain = new DrugQuantityTemp()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
