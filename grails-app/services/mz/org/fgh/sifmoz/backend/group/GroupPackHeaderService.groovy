package mz.org.fgh.sifmoz.backend.group

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(GroupPackHeader)
abstract class GroupPackHeaderService implements IGroupPackHeaderService{

}
