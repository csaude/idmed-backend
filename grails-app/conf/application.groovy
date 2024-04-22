
// AuditLog Plugin config
grails.plugin.auditLog.auditDomainClassName = 'mz.org.fgh.sifmoz.backend.auditTrail.AuditTrail'
grails.plugin.auditLog.verbose = false
grails.plugin.auditLog.verboseEvents = [AuditEventType.UPDATE, AuditEventType.INSERT]
grails.plugin.auditLog.failOnError = true
grails.plugin.auditLog.excluded = ['version', 'lastUpdated', 'lastUpdatedBy']
grails.plugin.auditLog.mask = ['password']
grails.plugin.auditLog.logIds = true
grails.plugin.auditLog.stampEnabled = false

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'mz.org.fgh.sifmoz.backend.protection.SecUser'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'mz.org.fgh.sifmoz.backend.protection.SecUserRole'
grails.plugin.springsecurity.authority.className = 'mz.org.fgh.sifmoz.backend.protection.Role'
grails.plugin.springsecurity.requestMap.className = 'mz.org.fgh.sifmoz.backend.protection.Requestmap'
grails.plugin.springsecurity.securityConfigType = 'Requestmap'
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/auth', 			 filters: 'none'],
	[pattern: '/**',             filters: 'JOINED_FILTERS']
]




grails.plugin.springsecurity.password.algorithm = 'bcrypt'
grails.plugin.springsecurity.successHandler.defaultTargetUrl = '/'
grails.plugin.springsecurity.rest.token.storage.useJwt=true
grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=true
grails.plugin.springsecurity.rest.token.storage.jwt.useEncryptedJwt = true
grails.plugin.springsecurity.rest.token.storage.jwt.secret = 'edK2k1P0D4770W56B6Rckf1TErImPWcu'

grails.plugin.springsecurity.rest.token.validation.useBearerToken = false
grails.plugin.springsecurity.rest.token.validation.headerName = 'X-Auth-Token'

grails.plugin.springsecurity.rest.token.rendering.usernamePropertyName='id'

grails.plugin.springsecurity.rest.logout.endpointUrl = '/api/logout'

grails.plugin.springsecurity.logout.postOnly = false

grails.plugin.springsecurity.rejectIfNoRule = true
grails.plugin.springsecurity.fii.rejectPublicInvocations = false
grails.plugin.springsecurity.useSecurityEventListener = true

grails.plugin.springsecurity.password.bcrypt.logrounds = 12

grails.plugin.springsecurity.rest.token.storage.jwt.expiration=36000000

grails.plugin.springsecurity.auth.loginFormUrl = '/'

grails.plugin.springsecurity.onAbstractAuthenticationFailureEvent = { event, appCtx ->

// Action to be performed (Example : send e-mail)

}