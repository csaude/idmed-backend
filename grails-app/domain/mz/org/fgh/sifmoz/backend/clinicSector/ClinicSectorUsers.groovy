package mz.org.fgh.sifmoz.backend.clinicSector

import grails.gorm.DetachedCriteria
import groovy.transform.ToString
import mz.org.fgh.sifmoz.backend.protection.SecUser
import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class ClinicSectorUsers implements Serializable {

    private static final long serialVersionUID = 1

    SecUser secUser
    ClinicSector clinicSector

    @Override
    boolean equals(other) {
        if (other instanceof ClinicSectorUsers) {
            other.secUserId == secUser?.id && other.clinicSectorId == clinicSector?.id
        }
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (secUser) {
            hashCode = HashCodeHelper.updateHash(hashCode, secUser.id)
        }
        if (clinicSector) {
            hashCode = HashCodeHelper.updateHash(hashCode, clinicSector.id)
        }
        hashCode
    }

    static ClinicSectorUsers get(long secUserId, String clinicSectorId) {
        criteriaFor(secUserId, clinicSectorId).get()
    }

    static boolean exists(long secUserId, String clinicSectorId) {
        criteriaFor(secUserId, clinicSectorId).count()
    }

    private static DetachedCriteria criteriaFor(long secUserId, String clinicSectorId) {
        ClinicSectorUsers.where {
            secUser == SecUser.load(secUserId) &&
                    clinicSector == ClinicSector.load(clinicSectorId)
        }
    }

    static ClinicSectorUsers create(SecUser secUser, ClinicSector clinicSector1, boolean flush = false) {
        def instance = new ClinicSectorUsers(secUser: secUser, clinicSector: clinicSector1)
        instance.save(flush: flush)
        instance
    }

    static boolean remove(SecUser u, ClinicSector r) {
        if (u != null && r != null) {
            ClinicSectorUsers.where { secUser == u && clinicSector == r }.deleteAll()
        }
    }

    static int removeAll(SecUser u) {
        u == null ? 0 : ClinicSectorUsers.where { secUser == u }.deleteAll() as int
    }

    static int removeAll(ClinicSector r) {
        r == null ? 0 : ClinicSectorUsers.where { clinicSector == r }.deleteAll() as int
    }

    static constraints = {
        secUser nullable: false
        clinicSector nullable: false, validator: { ClinicSector r, ClinicSectorUsers ur ->
            if (ur.secUser?.id) {
                if (ClinicSectorUsers.exists(ur.secUser.id, r.id)) {
                    return ['userSector.exists']
                }
            }
        }
    }

    static mapping = {
        id composite: ['secUser', 'clinicSector']
        version false
    }
}