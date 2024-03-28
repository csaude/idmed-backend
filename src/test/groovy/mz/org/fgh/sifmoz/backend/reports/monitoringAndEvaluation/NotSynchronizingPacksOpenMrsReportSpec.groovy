package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class NotSynchronizingPacksOpenMrsReportSpec extends Specification implements DomainUnitTest<NotSynchronizingPacksOpenMrsReport> {

     void "test domain constraints"() {
        when:
        NotSynchronizingPacksOpenMrsReport domain = new NotSynchronizingPacksOpenMrsReport()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
