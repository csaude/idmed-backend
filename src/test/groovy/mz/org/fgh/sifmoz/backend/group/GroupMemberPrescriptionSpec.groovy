package mz.org.fgh.sifmoz.backend.group

import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class GroupMemberPrescriptionSpec extends Specification implements DomainUnitTest<GroupMemberPrescription> {

     void "test domain constraints"() {
        when:
        GroupMemberPrescription domain = new GroupMemberPrescription()
        //TODO: Set domain props here

        then:
        domain.validate()
     }
}
