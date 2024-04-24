package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import org.springframework.scheduling.annotation.Scheduled

@Transactional
@Service(Prescription)
abstract class SecUserService implements ISecUserService {

    ISecUserService secUserService

    private static final int DEFAULT_FALLBACK_EXPIRE_DAYS = 90

    @Scheduled(cron = "0 0 12 * * 1,5")
    void schedulerUserMonitoringRunning() {
        SecUser.withTransaction {
            suspendInactiveUserAccounts();
        }
    }

    void suspendInactiveUserAccounts() {
        List<SecUser> secUserList = findAllUsersWithLastLoginBefore(getExpireDate());
        for (SecUser user : secUserList) {
            expireAndLockAccount(user)
        }
    }

    Date getExpireDate() {
        SystemConfigs systemConfigs = SystemConfigs.findWhere(key: "MAX_ACTIVE_DAYS_WITHOUT_LOGIN");
        def today = new Date();
        if (systemConfigs) {
            return today - Integer.parseInt(systemConfigs.value)
        } else {
            return today - DEFAULT_FALLBACK_EXPIRE_DAYS
        }
    }

    List<SecUser> findAllUsersWithLastLoginBefore(Date expireDate) {
        return SecUser.findAllByLastLoginLessThanEquals(expireDate)
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

    SecUser saveSecUserAndRoles(SecUser secUser, List<Role> roles) {
        if (secUser.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUser.errors
            return
        }

        secUserService.save(secUser)
        for (Role role : roles) {
            SecUserRole.create secUser, role
        }

    }
}
