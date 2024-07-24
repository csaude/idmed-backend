package mz.org.fgh.sifmoz.backend.protection

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.utilities.IRoleMenu

@GrailsCompileStatic
@EqualsAndHashCode(includes='authority')
@ToString(includes='authority', includeNames=true, includePackage=false)
class Role implements Serializable, IRoleMenu  {

	private static final long serialVersionUID = 1
	public static final String  administrationMenuCode = "06";
	public static final String  homeMenuCode = "08";

	String authority
	String name
	String description
	boolean active


	static hasMany = [menus: Menu]

	Role(String authority,String name,String description, boolean active){
		this()
		this.authority = authority
		this.name = name
		this.description = description
		this.active = active
		this.menus = new HashSet<>()
	}

	static constraints = {
		authority nullable: false, blank: false, unique: true
		name nullable: false, blank: false, unique: true
		description nullable: false, blank: false, unique: true
	}

	static mapping = {
		cache true
		menus joinTable: [name:"role_menu", key:"roles_id", column:"menus_id"]
		datasource 'ALL'
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
