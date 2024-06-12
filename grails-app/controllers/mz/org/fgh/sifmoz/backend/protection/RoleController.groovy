package mz.org.fgh.sifmoz.backend.protection

import grails.artefact.DomainClass
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.apache.commons.lang.StringUtils
import org.apache.commons.text.CaseUtils

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class RoleController extends RestfulController {

    RoleService roleService

    RequestmapService requestmapService

    GrailsApplication grailsApplication
    SpringSecurityService springSecurityService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    public static final String stockMenuCode = "03";
    public static final String dashboardMenuCode = "04";

    RoleController() {
        super(Role)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(roleService.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(roleService.get(id)) as JSON
    }

    @Transactional
    def save(Role role) {
        if (role == null) {
            render status: NOT_FOUND
            return
        }
        if (role.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond role.errors
            return
        }

        try {

            if (role.active == true) {
                role.menus.add(Menu.findByCode("08"))
                for (Menu menu : role.menus) {
                    List<Class> clazzes = grailsApplication.getArtefacts("Domain")*.clazz

                    for (Class clazz : clazzes) {
                        if (!java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()) && clazz.newInstance() instanceof BaseEntity) {
                            List<Menu> menus = clazz.newInstance().hasMenus()
                            if (menus.contains(menu)) {
                                //   String name = '%'+clazz.simpleName+'%'
                                //  String camelSimpleName = CaseUtils.toCamelCase(clazz.simpleName,false)
                                saveRequestMaps(role, clazz.simpleName)
                            }
                        } else if (clazz.simpleName == 'Role' || clazz.simpleName == 'SecUser') {
                            print(clazz.simpleName)
                            List<Menu> menus = clazz.newInstance().hasMenus()
                            if (menus.contains(menu)) {
                                //   String name = '%'+clazz.simpleName+'%'
                                //  String camelSimpleName = CaseUtils.toCamelCase(clazz.simpleName,false)
                                saveRequestMaps(role, clazz.simpleName)
                            }
                        }
                    }
                    if (menu.code == stockMenuCode || menu.code == dashboardMenuCode) {
                        Arrays.asList('dashBoard', 'drugStockFile').each { it ->
                            saveRequestMaps(role, it)
                        }
                    }
                }
            }
            roleService.save(role)

        } catch (ValidationException e) {
            respond role.errors
            return
        }
        springSecurityService.clearCachedRequestmaps()
        respond role, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {

        Role role
        def objectJSON = request.JSON

        println(objectJSON)

        if (objectJSON.id) {
            role = Role.get(objectJSON.id)
            if (role == null) {
                render status: NOT_FOUND
                return
            }
            role.properties = objectJSON
//                role.menus.eachWithIndex {menu, index ->
//                    menu.id = UUID.fromString(objectJSON.menus[index].id)
//                }
        }
        if (role.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond role.errors
            return
        }

        try {
            roleService.save(role)
        } catch (ValidationException e) {
            respond role.errors
            return
        }

        respond role, [status: OK, view: "show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || roleService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    private saveRequestMaps(Role role, String simpleName) {
        def name = '%' + '/api' + '/' + simpleName + '/**' + '%'

        if (!simpleName.equalsIgnoreCase("clinic") && !simpleName.equalsIgnoreCase("province") &&
                !simpleName.equalsIgnoreCase("district") && !simpleName.equalsIgnoreCase("menu") &&
                !simpleName.equalsIgnoreCase("drug") && !simpleName.equalsIgnoreCase("therapeuticRegimen") &&
                !simpleName.equalsIgnoreCase("identifierType")) {

            Requestmap requestMap = Requestmap.findByUrlIlike(name)
            println(name)
           if (requestMap) {
               String actualConfigAttribute = requestMap.configAttribute
               if (!actualConfigAttribute.contains(role.authority)) {
                   String newConfigAttribute = role.authority + "," + actualConfigAttribute
                   requestMap.setConfigAttribute(newConfigAttribute)
                   requestmapService.save(requestMap)
               }
           }
        }
    }
}
