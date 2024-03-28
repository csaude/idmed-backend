package mz.org.fgh.sifmoz.backend.group

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupInfoSpec extends Specification implements DomainUnitTest<GroupInfo> {

     void "test domain constraints"() {
        when:
        GroupInfo domain = new GroupInfo()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
