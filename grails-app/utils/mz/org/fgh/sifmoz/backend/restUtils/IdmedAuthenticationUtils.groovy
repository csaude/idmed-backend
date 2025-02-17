package mz.org.fgh.sifmoz.backend.restUtils


import grails.gorm.transactions.Transactional
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Log
import mz.org.fgh.sifmoz.backend.patientVisit.ExternalPatientVisit
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpStatus

class IdmedAuthenticationUtils {
    def grailsApplication
    private String authToken
    private Date tokenExpiration
    static Logger logger = LogManager.getLogger(IdmedAuthenticationUtils.class)
    static final String HTTP_REQUEST_METHOD_POST = 'POST'
    static final String HTTP_REQUEST_METHOD_GET = 'GET'


    private boolean isTokenValid() {
        return authToken && tokenExpiration && tokenExpiration > new Date()
    }

    String getAuthToken() {
        if (!isTokenValid()) {
            authenticate()
        }
        return authToken
    }

    private void authenticate() {
        def backend2Url = grailsApplication.config.datasource.url
        def credentials = [
                username: grailsApplication.config.datasource.useraccess,
                password:grailsApplication.config.dataSource.userpassaccess
        ]

        try {
            def connection = new URL("${backend2Url}/api/login").openConnection()
            connection.setRequestMethod('POST')
            connection.setRequestProperty('Content-Type', 'application/json')
            connection.doOutput = true

            def jsonBuilder = new JsonBuilder(credentials)
            connection.outputStream.write(jsonBuilder.toString().bytes)

            def response = connection.inputStream.text
            def jsonSlurper = new JsonSlurper()
            def result = jsonSlurper.parseText(response)

            authToken = result.access_token
            // Set token expiration (adjust based on backend2's token expiration)
            tokenExpiration = new Date() + 1 // Add 1 hour

        } catch (Exception e) {
            logger.error("Authentication failed: ${e.message}")
            throw new RuntimeException("Failed to authenticate with backend2: ${e.message}")
        }
    }

    @Transactional
    def syncExternalPatientVisit(ProvincialServer  provincialServer, ExternalPatientVisit externalPatientVisit) {
        def backend2Url = provincialServer.urlPath
        try {
            // Get fresh auth token
            def authToken = getAuthToken()
            def jsonBuilder = new JsonBuilder(externalPatientVisit)
            def connection = new URL("${backend2Url}/api/externalPatientVisit").openConnection()

            def responseCode = applyPOSTMethod(connection, jsonBuilder, authToken, HTTP_REQUEST_METHOD_POST)

            if (responseCode == HttpStatus.UNAUTHORIZED.value()) {
                // Token might have expired, force refresh and retry once
                authenticate()
                authToken = getAuthToken()

                // Retry the request with new token
                connection = new URL("${backend2Url}/api/externalPatientVisit").openConnection()

                responseCode = applyPOSTMethod(connection, jsonBuilder, authToken,HTTP_REQUEST_METHOD_POST)
            }

            return responseCode

        } catch (Exception e) {
           e.printStackTrace()
        }
    }

    static def applyPOSTMethod(URLConnection connection, JsonBuilder jsonBuilder, String authToken, String httpRequestMethod){
        connection.setRequestMethod(httpRequestMethod)
        connection.setRequestProperty('Content-Type', 'application/json')
        connection.setRequestProperty('X-Auth-Token', "${authToken}")
//            connection.setRequestProperty('Authorization', "Bearer ${authToken}")
        connection.doOutput = true
        connection.outputStream.write(jsonBuilder.toString().bytes)

        return connection.responseCode
    }
}

