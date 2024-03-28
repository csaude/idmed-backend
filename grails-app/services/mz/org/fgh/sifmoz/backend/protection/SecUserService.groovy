package mz.org.fgh.sifmoz.backend.protection

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription

@Transactional
@Service(Prescription)
abstract class SecUserService implements ISecUserService{

    ISecUserService secUserService

    SecUser saveSecUserAndRoles (SecUser secUser,List<Role> roles) {
        if (secUser.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond secUser.errors
            return
        }

        secUserService.save(secUser)
        for(Role role :roles)  {
            SecUserRole.create secUser, role
        }

    }
}
