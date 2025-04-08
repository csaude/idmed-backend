package mz.org.fgh.sifmoz.backend.interoperabilityTransationLog

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class InteroperabilityTransationLogControllerSpec extends Specification implements ControllerUnitTest<InteroperabilityTransationLogController> {

     void "test index action"() {
        when:
        controller.index()

        then:
        status == 200

     }
}
