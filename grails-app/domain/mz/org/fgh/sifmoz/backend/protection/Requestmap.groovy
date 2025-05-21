package mz.org.fgh.sifmoz.backend.protection

import mz.org.fgh.sifmoz.backend.utilities.IRoleMenu
import org.springframework.http.HttpMethod

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@EqualsAndHashCode(includes=['configAttribute', 'httpMethod', 'url'])
@ToString(includes=['configAttribute', 'httpMethod', 'url'], cache=true, includeNames=true, includePackage=false)
class Requestmap implements Serializable, IRoleMenu {

	public static final String  administrationMenuCode = "06";
	public static final String  homeMenuCode = "08";
	private static final long serialVersionUID = 1

	String configAttribute
	HttpMethod httpMethod
	String url

	static constraints = {
		configAttribute nullable: false, blank: false
		httpMethod nullable: true
		url nullable: false, blank: false, unique: 'httpMethod'
	}

	static mapping = {
		cache true
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
