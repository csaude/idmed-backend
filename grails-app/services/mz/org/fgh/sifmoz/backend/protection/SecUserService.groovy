package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.DetachedCriteria
import grails.gorm.services.Service
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Service
@Slf4j
@CompileStatic
@EnableScheduling
class SecUserService {

    static lazyInit = false
    private static final int DEFAULT_FALLBACK_EXPIRE_DAYS = 90

    @Scheduled(cron = "0 0 12 * * 1,5")
    void schedulerUserMonitoringRunning() {
        println('CRON FOR USERS UPDDATE')
        SecUser.withTransaction {
            suspendInactiveUserAccounts()
        }

    }

    void suspendInactiveUserAccounts() {
        List<SecUser> secUserList = findAllUsersWithLastLoginBefore(getExpireDate())
        for (SecUser user : secUserList) {
            if(!user.username.equalsIgnoreCase('iDMED') &&
                    !user.username.equalsIgnoreCase('admin'))
            expireAndLockAccount(user)
        }
    }

    Date getExpireDate() {
        SystemConfigs systemConfigs = SystemConfigs.findWhere(key: 'MAX_ACTIVE_DAYS_WITHOUT_LOGIN')
        Calendar cal = Calendar.getInstance()
        int expireDays = systemConfigs ? Integer.parseInt(systemConfigs.value) : DEFAULT_FALLBACK_EXPIRE_DAYS
        cal.add(Calendar.DATE, -expireDays)
        Date expireDate = cal.getTime()

        return expireDate
    }

    List<SecUser> findAllUsersWithLastLoginBefore(Date expireDate) {
        DetachedCriteria<SecUser> criteria = new DetachedCriteria(SecUser).build {
            le('lastLogin', expireDate)
            eq('accountLocked', false)
            ne('username','iDMED')
            ne('username','admin')
        }
        return criteria.list()
    }

    void expireAndLockAccount(SecUser user) {
        try {
            user.accountLocked = true
            user.accountExpired = true
            user.passwordExpired = true
            user.enabled = false
            user.save()
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

//    SecUser saveSecUserAndRoles(SecUser secUser, List<Role> roles) {
//        if (secUser.hasErrors()) {
//            transactionStatus.setRollbackOnly()
//            respond secUser.errors
//            return
//        }
//
//        secUser.save()
//        for (Role role : roles) {
//            SecUserRole.create secUser, role
//        }
//
//    }
}
