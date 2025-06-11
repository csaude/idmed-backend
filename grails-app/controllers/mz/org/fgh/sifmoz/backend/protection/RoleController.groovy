package mz.org.fgh.sifmoz.backend.protection

import grails.artefact.DomainClass
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityService
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.stocklevel.StockLevel
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.apache.commons.lang.StringUtils
import org.apache.commons.text.CaseUtils
import org.pac4j.core.context.HttpConstants
import org.springframework.http.HttpMethod

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

    def sectionMap = [
            "Prescription": ["PatientVisit", "Pack", "Prescription","PrescriptionDetail" , "PatientVisitDetails","PrescribedDrug","PackagedDrug","PackagedDrugStock" ],
            "Pack": ["PatientVisit", "Pack", "Prescription","PrescriptionDetail" , "PatientVisitDetails","PrescribedDrug","PackagedDrug","PackagedDrugStock" ],
            "PharmaceuticalAttention": ["Tbscreening", "VitalSignsScreening", "AdherenceScreening","RamScreenings" , "PregnancyScreenings","PatientVisit"],
            "Patient": ["Patient", "PostoAdministrativo","Localidade"],
            "Episode": ["Episode"],
            "PatientServiceIdentifier": ["PatientServiceIdentifier"],
            "Stock": ["Stock","StockEntrance","StockAdjustment","StockCenter"],
            "Inventory": ["Stock","StockEntrance","StockAdjustment","StockCenter", "Inventory"],
            "Distribution": ["Stock", "StockEntrance", "StockAdjustment", "StockCenter", "Inventory", "StockDistributor", "StockDistributorBatch","StockLevel"],
    ]

    def sectionMethodActions = [
            "Prescription": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.POST): ["remove"]
            ],
            "Pack": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.POST): ["remove"]
            ],
            "PharmaceuticalAttention": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.DELETE): ["remove"]
            ],
            "Patient": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"]
            ],
            "Episode": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"],
                    (HttpMethod.PATCH): ["remove"]
            ],
            "PatientServiceIdentifier": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"]
            ],
            "Stock": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"]
            ],
            "Inventory": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"]
            ],
            "Distribution": [
                    (HttpMethod.POST): ["add"],
                    (HttpMethod.PUT): ["edit"],
                    (HttpMethod.PATCH): ["edit"]
            ]
    ]

    def excludedSimpleNames = [
            "clinic", "province", "district", "menu", "drug",
            "therapeuticRegimen", "identifierType"
    ].collect { it.toLowerCase() }


    static final List<HttpMethod> HTTP_METHODS = [HttpMethod.POST, HttpMethod.PUT, HttpMethod.GET, HttpMethod.DELETE,
                                                  HttpMethod.PATCH]
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
                                role.uiSections.forEach { section ->
                                    saveRequestMaps(role, clazz.simpleName,section)
                                }

                            }
                        } else if (clazz.simpleName == 'Role' || clazz.simpleName == 'SecUser' || clazz.simpleName == 'Requestmap') {
                            List<Menu> menus = clazz.newInstance().hasMenus()
                            if (menus.contains(menu)) {
                                //   String name = '%'+clazz.simpleName+'%'
                                //  String camelSimpleName = CaseUtils.toCamelCase(clazz.simpleName,false)
                                saveRequestMaps(role, clazz.simpleName,null)
                            }
                        }
                    }
                    if (menu.code == stockMenuCode || menu.code == dashboardMenuCode) {
                        Arrays.asList('dashBoard', 'drugStockFile').each { it ->
                            saveRequestMaps(role, it,null)
                        }
                    }
                }
                for (UiSection uiSection : role.uiSections) {
                    saveRequestMapsUiSection(role,uiSection)
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
        Role updatedRole = objectJSON

        if (objectJSON.id) {
            role = Role.get(objectJSON.id)
            if (role == null) {
                render status: NOT_FOUND
                return
            }
          //  role.properties = objectJSON
//                role.menus.eachWithIndex {menu, index ->
//                    menu.id = UUID.fromString(objectJSON.menus[index].id)
//                }

            Menu mandatoryMenu = Menu.findByCode("08")
            if (!role.menus.contains(mandatoryMenu)) {
                role.menus.add(mandatoryMenu)
            }

            Set<Menu> addedMenus = new HashSet<>(updatedRole.menus)
            addedMenus.removeAll(role.menus)

            Set<Menu> removedMenus = new HashSet<>(role.menus)
            removedMenus.removeAll(updatedRole.menus)


            Set<UiSection> addedUiSections = new HashSet<>(updatedRole.uiSections)
            addedUiSections.removeAll(role.uiSections)

            Set<UiSection> removedUiSections = new HashSet<>(role.uiSections)
            removedUiSections.removeAll(updatedRole.uiSections)

            // Process added menus
            for (Menu menu : addedMenus) {
                processMenuAddition(role, menu)
            }

            // Process added UI sections
            for (UiSection uiSection : addedUiSections) {
                saveRequestMapsUiSection(role, uiSection)
            }

            // Process removed menus
            for (Menu menu : removedMenus) {
                processMenuRemoval(role, menu)
            }

            // Process removed UI sections
            for (UiSection uiSection : removedUiSections) {
               removeUiSectionRequestMaps(role, uiSection)
            }
        }
        role.id = objectJSON.id
        role.description = updatedRole.description
        role.authority = updatedRole.authority
        role.active = updatedRole.active
        role.name = updatedRole.name
        role.menus = updatedRole.menus
        role.uiSections = updatedRole.uiSections
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





    private void processMenuAddition(Role role, Menu menu) {
        List<Class> clazzes = grailsApplication.getArtefacts("Domain")*.clazz

        // Process domain class permissions
        for (Class clazz : clazzes) {
            try {
                if (!java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()) &&
                        clazz.newInstance() instanceof BaseEntity) {
                    List<Menu> menus = clazz.newInstance().hasMenus()
                    if (menus.contains(menu)) {
                        role.uiSections.each { section ->
                            saveRequestMaps(role, clazz.simpleName, section)
                        }
                    }
                } else if (clazz.simpleName == 'Role' || clazz.simpleName == 'SecUser' || clazz.simpleName == 'Requestmap') {
                    List<Menu> menus = clazz.newInstance().hasMenus()
                    if (menus.contains(menu)) {
                        saveRequestMaps(role, clazz.simpleName, null)
                    }
                }
            } catch (Exception e) {
                log.error("Error processing domain class ${clazz.name} for menu ${menu.code}: ${e.message}", e)
            }
        }

        // Handle special cases for stock and dashboard menus
        if (menu.code == stockMenuCode || menu.code == dashboardMenuCode) {
            Arrays.asList('dashBoard', 'drugStockFile').each { it ->
                saveRequestMaps(role, it, null)
            }
        }
    }

    private void processMenuRemoval(Role role, Menu menu) {
        List<Class> clazzes = grailsApplication.getArtefacts("Domain")*.clazz

        // Process domain class permissions
        for (Class clazz : clazzes) {
            try {
                if (!java.lang.reflect.Modifier.isAbstract(clazz.getModifiers()) &&
                        clazz.newInstance() instanceof BaseEntity) {
                    List<Menu> menus = clazz.newInstance().hasMenus()
                    if (menus.contains(menu)) {
                        removeRequestMapsForDomainClass(role, clazz.simpleName)
                    }
                } else if (clazz.simpleName == 'Role' || clazz.simpleName == 'SecUser' || clazz.simpleName == 'Requestmap') {
                    List<Menu> menus = clazz.newInstance().hasMenus()
                    if (menus.contains(menu)) {
                        removeRequestMapsForDomainClass(role, clazz.simpleName)
                    }
                }
            } catch (Exception e) {
                log.error("Error processing domain class ${clazz.name} for menu removal ${menu.code}: ${e.message}", e)
            }
        }

        // Handle special cases for stock and dashboard menus
        if (menu.code == stockMenuCode || menu.code == dashboardMenuCode) {
            Arrays.asList('dashBoard', 'drugStockFile').each { it ->
                removeRequestMapsForDomainClass(role, it)
            }
        }
    }

/**
 * Remove the role from requestmaps for a specific domain class
 */
    private void removeRequestMapsForDomainClass(Role role, String simpleName) {
        def apiUrlPattern = '%/api/' + simpleName + '/**%'

        // Process each HTTP method
        for (HttpMethod method : HTTP_METHODS) {
            Requestmap requestMap = Requestmap.findByUrlIlikeAndHttpMethod(apiUrlPattern, method)
            if (requestMap) {
                removeRoleFromRequestMap(requestMap, role)
            }
        }
    }

/**
 * Remove the role from a requestmap's config attribute
 */
    private void removeRoleFromRequestMap(Requestmap requestMap, Role role) {
        String configAttribute = requestMap.configAttribute

        // Split by comma to get individual authorities
        List<String> authorities = configAttribute.split(',').collect { it.trim() }

        // Remove the role's authority
        authorities.removeAll { it == role.authority }

        if (authorities.isEmpty()) {
            // If no authorities left, delete the requestmap
            requestMap.delete(flush: true)
        } else {
            // Otherwise update with remaining authorities
            requestMap.configAttribute = authorities.join(',')
            requestmapService.save(requestMap)
        }
    }

/**
 * Remove requestmaps related to a UI section for a role
 */
    private void removeUiSectionRequestMaps(Role role, UiSection uiSection) {
        // Remove the role from the UI section's requestmap
        Requestmap requestMap = Requestmap.findByUrlIlike(uiSection.requestMapUrl)
        if (requestMap) {
            removeRoleFromRequestMap(requestMap, role)
        }

        // For each domain class in the section, remove role permissions
        if (sectionMap.containsKey(uiSection.category)) {
            sectionMap[uiSection.category].each { simpleName ->
                // Only remove for this HTTP method if the section/method/action combination is valid
                for (HttpMethod method : HTTP_METHODS) {
                    if (method != HttpMethod.GET && isSectionMethodActionValid(uiSection.category, method, uiSection.action)) {
                        def apiUrlPattern = '%/api/' + simpleName + '/**%'
                        Requestmap requestMap1 = Requestmap.findByUrlIlikeAndHttpMethod(apiUrlPattern, method)
                        if (requestMap1) {
                            removeRoleFromRequestMap(requestMap, role)
                        }
                    }
                }
            }
        }
    }

    private saveRequestMapsUiSection(Role role,UiSection uiSection) {
        Requestmap requestMap = Requestmap.findByUrlIlike(uiSection.requestMapUrl)
        if (requestMap) {
            String actualConfigAttribute = requestMap.configAttribute
            if (!actualConfigAttribute.contains(role.authority)) {
                String newConfigAttribute = role.authority + "," + actualConfigAttribute
                requestMap.setConfigAttribute(newConfigAttribute)
                requestmapService.save(requestMap)
            }
        }
    }

    private saveRequestMaps(Role role, String simpleName, UiSection uiSection) {
        // Skip processing for excluded simple names
        if (excludedSimpleNames.contains(simpleName.toLowerCase())) {
            return
        }

        def apiUrlPattern = '%/api/' + simpleName + '/**%'

        for (HttpMethod method : HTTP_METHODS) {
            // Handle GET method separately as it has different logic
            if (method == HttpMethod.GET) {
                updateRequestMapForMethod(apiUrlPattern, method, role)
                continue
            }

            // Skip if uiSection is not provided
            if (!uiSection) {
                continue
            }

            String sectionDescription = uiSection.category
            String action = uiSection.action

            // Check if this section supports this method and action
            if (isSectionMethodActionValid(sectionDescription, method, action)) {
                // Check if the simpleName is in the section's list
                if (isInSection(sectionDescription, simpleName)) {
                    updateRequestMapForMethod(apiUrlPattern, method, role)
                }
            }
        }
    }


    private boolean isSectionMethodActionValid(String sectionDescription, HttpMethod method, String action) {
        def methodActions = sectionMethodActions[sectionDescription]
        return methodActions && methodActions[method] && methodActions[method].contains(action)
    }

// Helper method to check if a simpleName is in a section's list
    private boolean isInSection(String sectionDescription, String simpleName) {
        return sectionMap.containsKey(sectionDescription) &&
                sectionMap[sectionDescription].contains(simpleName)
    }

// Helper method to update the request map for a specific URL and HTTP method
    private void updateRequestMapForMethod(String urlPattern, HttpMethod method, Role role) {
        Requestmap requestMap = Requestmap.findByUrlIlikeAndHttpMethod(urlPattern, method)
        if (requestMap) {
            String currentAuthority = requestMap.configAttribute
            if (!currentAuthority.contains(role.authority)) {
                String newAuthority = role.authority + "," + currentAuthority
                requestMap.setConfigAttribute(newAuthority)
                requestmapService.save(requestMap)
            }
        }
    }





}
