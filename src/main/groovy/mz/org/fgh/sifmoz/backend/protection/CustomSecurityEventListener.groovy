package mz.org.fgh.sifmoz.backend.protection

import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import org.springframework.context.ApplicationEvent
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent
import org.springframework.security.authentication.event.AuthenticationSuccessEvent
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.http.HttpSession
import java.util.logging.Logger

class CustomSecurityEventListener implements ApplicationListener<ApplicationEvent> {

    Logger log = Logger.getLogger(CustomSecurityEventListener.name)

    void onApplicationEvent(ApplicationEvent appEvent) {

        if (appEvent instanceof AuthenticationSuccessEvent) {
            handleSuccessEvent((AuthenticationSuccessEvent) appEvent)
        } else if (appEvent instanceof AuthenticationFailureBadCredentialsEvent) {
            handleFailureEvent((AuthenticationFailureBadCredentialsEvent) appEvent)
        }
    }

    void handleSuccessEvent(AuthenticationSuccessEvent event) {
        UserDetails userDetails = (UserDetails) event?.getAuthentication()?.getPrincipal()
        if (userDetails) {
            String username = userDetails.getUsername()
            handleSuccessEventForUser(username)
            log.info("username from CustomSecurityEventListener = ${username}")
        }
    }

    void handleSuccessEventForUser(String username) {
        SecUser.withTransaction {
            SecUser secUser = SecUser.findWhere(username: username)
            SystemConfigs systemConfigs = SystemConfigs.findWhere(key: "MAX_LOGIN_TRIES")
            if (secUser && systemConfigs)
                secUser.loginRetries = Integer.parseInt(systemConfigs.value)
            else
                secUser.loginRetries = 3
            secUser.save()

        }
    }

    void handleFailureEvent(AuthenticationFailureBadCredentialsEvent event) {
        def username = event?.getSource()?.getAt('principal')
        if (username) {
            handleFailureEventForUser(username)
        }
    }

    void handleFailureEventForUser(String username) {
        SecUser.withTransaction {
            SecUser secUser = SecUser.findWhere(username: username)
            if (secUser) {
                updateLoginRetriesForUser(secUser)
                secUser.save()
            }
        }
    }

    void updateLoginRetriesForUser(SecUser secUser) {
        if (secUser.loginRetries == 1) {
            secUser.accountLocked = true
        } else {
            secUser.loginRetries -= 1
        }
    }

    private HttpSession getSession() {
        return RequestContextHolder.currentRequestAttributes().getSession()
    }
}
