package mz.org.fgh.sifmoz.backend.group

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupPackHeaderSpec extends Specification implements DomainUnitTest<GroupPackHeader> {

     void "test domain constraints"() {
        when:
        GroupPackHeader domain = new GroupPackHeader()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
