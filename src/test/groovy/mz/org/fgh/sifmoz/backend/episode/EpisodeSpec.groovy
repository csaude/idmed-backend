package mz.org.fgh.sifmoz.backend.episode

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class EpisodeSpec extends Specification implements DomainUnitTest<Episode> {

     void "test domain constraints"() {
        when:
        Episode domain = new Episode()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
