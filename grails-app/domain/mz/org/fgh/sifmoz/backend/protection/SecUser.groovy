package mz.org.fgh.sifmoz.backend.protection

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.utilities.IRoleMenu

@GrailsCompileStatic
@EqualsAndHashCode(includes='username')
@ToString(includes='username', includeNames=true, includePackage=false)
class SecUser implements Serializable, IRoleMenu {

    private static final long serialVersionUID = 1
    public static final String  administrationMenuCode = "06";
    public static final String  homeMenuCode = "08";

    String username
    String password
    String openmrsPassword
    String fullName
    String contact
    String email
    boolean enabled = true
    boolean accountExpired
    boolean accountLocked
    boolean passwordExpired
    String[] roles
    int loginRetries
    Date lastLogin = new Date()

    static transients = ['roles']

    static hasMany = [clinics: Clinic, clinicSectors: ClinicSector]

    SecUser(String username,String password, String fullname, String contact,String email, String openmrsPassword){
        this()
        this.username = username
        this.password = password
        this.openmrsPassword = openmrsPassword
        this.fullName = fullname
        this.contact = contact
        this.email = email
    }

    Set<Role> getAuthorities() {
        (SecUserRole.findAllBySecUser(this) as List<SecUserRole>)*.role as Set<Role>
    }

    static constraints = {
        password nullable: false, blank: false, password: true
        username nullable: false, blank: false, unique: true
        email nullable: true, blank: true
        openmrsPassword nullable: true, blank: true
        contact nullable: true, blank: true
        roles bindable: true
        clinicSectors bindable: true
        loginRetries nullable: false
        lastLogin nullable: false

    }

    static mapping = {
	    password column: '`password`'
        clinics  joinTable: [name: "clinic_users", key: "sec_user_id", column: "clinic_id"]
        id column: 'id', index: 'Pk_SecUser_Idx'
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(administrationMenuCode,homeMenuCode))
        }
        return menus
    }
}
