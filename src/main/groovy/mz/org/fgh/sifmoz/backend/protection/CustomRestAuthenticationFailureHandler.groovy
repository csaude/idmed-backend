package mz.org.fgh.sifmoz.backend.protection

import groovy.json.JsonBuilder
import org.grails.plugins.web.taglib.ValidationTagLib
import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CustomRestAuthenticationFailureHandler implements AuthenticationFailureHandler {


    @Override
    void onAuthenticationFailure(HttpServletRequest request,
                                 HttpServletResponse response,
                                 AuthenticationException exception)
            throws IOException, ServletException {

        def msg = ""
        def g = new ValidationTagLib()

        response.setStatus(401)
        response.addHeader('WWW-Authenticate', 'Bearer')
        if (exception instanceof AccountExpiredException) {
            msg = g.message(code: "springSecurity.errors.login.expired")
        } else if (exception instanceof CredentialsExpiredException) {
            msg = g.message(code: "springSecurity.errors.login.passwordExpired")
        } else if (exception instanceof DisabledException) {
            msg = g.message(code: "springSecurity.errors.login.disabled")
        } else if (exception instanceof LockedException) {
            msg = g.message(code: "springSecurity.errors.login.locked")
        } else if(exception instanceof BadCredentialsException){
            msg = g.message(code: "springSecurity.errors.login.badCredencial")
        } else
            msg = g.message(code: "springSecurity.errors.login.fail")
        PrintWriter out = response.getWriter()
        response.setContentType("aplication/json")
        response.setCharacterEncoding("UTF-8")
        out.print(new JsonBuilder([message: msg]).toString())
        out.flush()
    }
}
