package mz.org.fgh.sifmoz.backend.patientVisit

import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class RestExternalPatientVisitControllerSpec extends Specification implements ControllerUnitTest<RestExternalPatientVisitController> {

     void "test index action"() {
        when:
        controller.index()

        then:
        status == 200

     }
}
