package mz.org.fgh.sifmoz.backend.groupType

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupTypeSpec extends Specification implements DomainUnitTest<GroupType> {

     void "test domain constraints"() {
        when:
        GroupType domain = new GroupType()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
