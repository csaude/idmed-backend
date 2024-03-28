package mz.org.fgh.sifmoz.backend.migration.stage

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class MigrationStageSpec extends Specification implements DomainUnitTest<MigrationStage> {

     void "test domain constraints"() {
        when:
        MigrationStage domain = new MigrationStage()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
