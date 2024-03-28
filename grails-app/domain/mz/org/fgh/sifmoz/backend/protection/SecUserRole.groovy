package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.DetachedCriteria
import groovy.transform.ToString

import org.codehaus.groovy.util.HashCodeHelper
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@ToString(cache=true, includeNames=true, includePackage=false)
class SecUserRole implements Serializable {

	private static final long serialVersionUID = 1

	SecUser secUser
	Role role

	@Override
	boolean equals(other) {
		if (other instanceof SecUserRole) {
			other.secUserId == secUser?.id && other.roleId == role?.id
		}
	}

    @Override
	int hashCode() {
	    int hashCode = HashCodeHelper.initHash()
        if (secUser) {
            hashCode = HashCodeHelper.updateHash(hashCode, secUser.id)
		}
		if (role) {
		    hashCode = HashCodeHelper.updateHash(hashCode, role.id)
		}
		hashCode
	}

	static SecUserRole get(long secUserId, long roleId) {
		criteriaFor(secUserId, roleId).get()
	}

	static boolean exists(long secUserId, long roleId) {
		criteriaFor(secUserId, roleId).count()
	}

	private static DetachedCriteria criteriaFor(long secUserId, long roleId) {
		SecUserRole.where {
			secUser == SecUser.load(secUserId) &&
			role == Role.load(roleId)
		}
	}

	static SecUserRole create(SecUser secUser, Role role, boolean flush = false) {
		def instance = new SecUserRole(secUser: secUser, role: role)
		instance.save(flush: flush)
		instance
	}

	static boolean remove(SecUser u, Role r) {
		if (u != null && r != null) {
			SecUserRole.where { secUser == u && role == r }.deleteAll()
		}
	}

	static int removeAll(SecUser u) {
		u == null ? 0 : SecUserRole.where { secUser == u }.deleteAll() as int
	}

	static int removeAll(Role r) {
		r == null ? 0 : SecUserRole.where { role == r }.deleteAll() as int
	}

	static constraints = {
	    secUser nullable: false
		role nullable: false, validator: { Role r, SecUserRole ur ->
			if (ur.secUser?.id) {
				if (SecUserRole.exists(ur.secUser.id, r.id)) {
				    return ['userRole.exists']
				}
			}
		}
	}

	static mapping = {
		id composite: ['secUser', 'role']
		version false
	}
}
