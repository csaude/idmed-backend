package mz.org.fgh.sifmoz.backend.episodeType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class EpisodeTypeSpec extends Specification implements DomainUnitTest<EpisodeType> {

     void "test domain constraints"() {
        when:
        EpisodeType domain = new EpisodeType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
