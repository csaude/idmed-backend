package mz.org.fgh.sifmoz.backend.protection

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSectorUsers
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.interoperabilityAttribute.InteroperabilityAttribute
import mz.org.fgh.sifmoz.backend.interoperabilityType.InteroperabilityType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class SecUserController extends RestfulController {

//    ISecUserService secUserService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    SecUserController() {
        super(SecUser)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(SecUser.list(params)) as JSON
    }

    def show(Long id) {
        render JSONSerializer.setJsonObjectResponse(SecUser.get(id)) as JSON
    }

    @Transactional
    def save(SecUser secUser) {
        if (secUser == null) {
            render status: NOT_FOUND
            return
        }
        if (secUser.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUser.errors
            return
        }

        try {
            secUser.save()
                    if( secUser.roles != null && secUser.accountLocked == false && secUser.roles.length > 0) {
                        SecUserRole.removeAll(secUser)
                        for(String role : secUser.roles) {
                            Role secRole= Role.findByAuthority(role)
                            SecUserRole.create(secUser, secRole)
                        }
                    }
            /*
            if( secUser.clinics != null && secUser.accountLocked == false) {
             //   ClinicSectorUsers.removeAll(secUser)
                for(Clinic clinicSector : secUser.clinics) {
                  //  ClinicSector secSector= ClinicSector.get(clinicSector.id)
                  //  ClinicSectorUsers.create(secUser, secSector)
                }
            }
             */
        } catch (ValidationException e) {
            respond secUser.errors
            return
        }

        render JSONSerializer.setJsonObjectResponse(SecUser.get(secUser.id)) as JSON
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        def SecUserFromJSON = (parseTo(objectJSON.toString()) as Map) as SecUser
        SecUser secUser = SecUser.get(objectJSON.id)

        secUser.username = SecUserFromJSON.username
        secUser.fullName = SecUserFromJSON.fullName
        secUser.contact = SecUserFromJSON.contact
        secUser.email = SecUserFromJSON.email
        secUser.enabled = SecUserFromJSON.enabled
        secUser.accountExpired = SecUserFromJSON.accountExpired
        secUser.accountLocked = SecUserFromJSON.accountLocked
        secUser.passwordExpired = SecUserFromJSON.passwordExpired
        secUser.roles = SecUserFromJSON.roles
        secUser.loginRetries = SecUserFromJSON.loginRetries
        secUser.lastLogin = SecUserFromJSON.lastLogin

        secUser.clinics = SecUserFromJSON.clinics
        if (secUser == null) {
            render status: NOT_FOUND
            return
        }
        if (secUser.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUser.errors
            return
        }

        try {
           if(secUser.enabled && !secUser.accountLocked ) {
               secUser.accountExpired = false
               secUser.passwordExpired = false
           }
            secUser.save()
            if (secUser.username == 'iDMED') {
                def userText = secUser.username + ':' + secUser.password
                byte[] bytes = userText.getBytes();
                String encoded = Base64.getEncoder().encodeToString(bytes);
                def healthInformationSystem = HealthInformationSystem.findByAbbreviation('OpenMRS')
                def interoperabilityType = InteroperabilityType.findByCode('OPENMRS_USER_PROVIDER_UUID')
                def interoperabilityAttribute = InteroperabilityAttribute.findByHealthInformationSystemAndInteroperabilityType(
                        healthInformationSystem,interoperabilityType)
                interoperabilityAttribute.value = encoded
                interoperabilityAttribute.save(flush:true)
            }
            if ( secUser.roles != null && secUser.accountLocked == false && secUser.roles.length > 0) {
                SecUserRole.removeAll(secUser)
                for(String role : secUser.roles) {
                    Role secRole= Role.findByAuthority(role)
                    SecUserRole.create(secUser, secRole)
                }
            }
            /*
            if( secUser.clinicSectors != null && secUser.accountLocked == false) {
                ClinicSectorUsers.removeAll(secUser)
                for (ClinicSector clinicSector : secUser.clinicSectors) {
                    ClinicSector secSector = ClinicSector.get(clinicSector.id)
                    ClinicSectorUsers.create(secUser, secSector)
                }
            }
             */
        } catch (ValidationException e) {
            respond secUser.errors
            return
        }

        render JSONSerializer.setJsonObjectResponse(SecUser.get(secUser.id)) as JSON
    }

    @Transactional
    def delete(Long id) {

        SecUser secUser = SecUser.get(secUser.id)

        if (id == null || secUser.delete() == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

//    @Transactional
//    void saveSecUserAndRoles(SecUser secUser,List<Role> roles) {
//
//      secUserService.saveSecUserAndRoles(secUser ,roles)
//
//        respond secUser, [status: CREATED, view:"show"]
//    }
    @Transactional
    void saveSecUserAndRoles(SecUser secUser, List<Role> roles) {
        if (secUser.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUser.errors
            return
        }

        secUser.save()
        for (Role role : roles) {
            SecUserRole.create secUser, role
        }
        respond secUser, [status: CREATED, view:"show"]
    }
}
