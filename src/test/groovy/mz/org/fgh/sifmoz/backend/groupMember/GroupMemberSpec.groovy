package mz.org.fgh.sifmoz.backend.groupMember

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupMemberSpec extends Specification implements DomainUnitTest<GroupMember> {

     void "test domain constraints"() {
        when:
        GroupMember domain = new GroupMember()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
