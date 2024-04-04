// AuditLog Plugin config
grails.plugin.auditLog.auditDomainClassName = 'mz.org.fgh.sifmoz.backend.auditTrail.AuditTrail'
grails.plugin.auditLog.verbose = true
grails.plugin.auditLog.verboseEvents = [AuditEventType.UPDATE, AuditEventType.INSERT]
grails.plugin.auditLog.failOnError = true
grails.plugin.auditLog.excluded = ['version', 'lastUpdated', 'lastUpdatedBy']
grails.plugin.auditLog.mask = ['password']
grails.plugin.auditLog.logIds = true
grails.plugin.auditLog.stampEnabled = false