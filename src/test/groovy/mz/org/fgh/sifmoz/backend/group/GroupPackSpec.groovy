package mz.org.fgh.sifmoz.backend.group

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupPackSpec extends Specification implements DomainUnitTest<GroupPack> {

     void "test domain constraints"() {
        when:
        GroupPack domain = new GroupPack()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
