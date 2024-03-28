package mz.org.fgh.sifmoz.backend.healthInformationSystem

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional


@Transactional
@Service(SystemConfigs)
abstract class SystemConfigsService implements ISystemConfigsService {

    @Override
    SystemConfigs getByKey(String key) {
      return SystemConfigs.findByKey(key)
    }
}
