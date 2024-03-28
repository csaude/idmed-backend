package mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class ArvDailyRegisterReportTempSpec extends Specification implements DomainUnitTest<ArvDailyRegisterReportTemp> {

     void "test domain constraints"() {
        when:
        ArvDailyRegisterReportTemp domain = new ArvDailyRegisterReportTemp()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
