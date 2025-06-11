package mz.org.fgh.sifmoz.backend.protection

import grails.converters.JSON
import grails.validation.ValidationException
import org.springframework.security.core.context.SecurityContextHolder
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK


import grails.gorm.transactions.ReadOnly
import grails.gorm.transactions.Transactional

@ReadOnly
class RequestmapController {

    RequestmapService requestmapService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]


    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond requestmapService.list(params), model:[requestmapCount: requestmapService.count()]
    }

    def show(Long id) {
        respond requestmapService.get(id)
    }

    @Transactional
    def save(Requestmap requestmap) {
        if (requestmap == null) {
            render status: NOT_FOUND
            return
        }
        if (requestmap.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond requestmap.errors
            return
        }

        try {
            requestmapService.save(requestmap)
        } catch (ValidationException e) {
            respond requestmap.errors
            return
        }

        respond requestmap, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(Requestmap requestmap) {
        if (requestmap == null) {
            render status: NOT_FOUND
            return
        }
        if (requestmap.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond requestmap.errors
            return
        }

        try {
            requestmapService.save(requestmap)
        } catch (ValidationException e) {
            respond requestmap.errors
            return
        }

        respond requestmap, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || requestmapService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getUserPermissions() {
        def authentication = SecurityContextHolder.context.authentication


        def authorities = authentication.authorities
        def userRoles = authorities.collect { it.authority }


        def requestMaps = Requestmap.list()

        def permissionMap = [:]
        def uiPermissions = [:]

        requestMaps.each { requestMap ->
            String url = requestMap.url
            String roles = requestMap.configAttribute

            if (url.startsWith('/ui/')) {
                def parts = url.split('/')
                if (parts.size() >= 4) {
                    def section = parts[2]  // 'prescription'
                    def action = parts[3]   // 'add', 'view', etc.


                    if (!uiPermissions[section]) {
                        uiPermissions[section] = [:]
                    }


                    def roleList = roles.split(',')
                    uiPermissions[section][action] = userRoles.any { userRole ->
                        roleList.any { it == userRole }
                    }

                }

            }


            permissionMap[url] = [
                    roles: roles.split(','),
                    hasAccess: userRoles.any { userRole ->
                        roles.split(',').any { it == userRole }
                    }
            ]
        }

        def permissionsPayload = [
                roles: userRoles,
                permissions: permissionMap,
                uiPermissions: uiPermissions
        ]
        render permissionsPayload as JSON
    }
}
