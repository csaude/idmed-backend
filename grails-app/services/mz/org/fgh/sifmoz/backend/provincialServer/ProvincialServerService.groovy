package mz.org.fgh.sifmoz.backend.provincialServer

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(ProvincialServer)
abstract class ProvincialServerService implements IProvincialServerService{

    @Override
    ProvincialServer getByCodeAndDestination(String code, String destination) {
        return ProvincialServer.findByCodeAndDestination(code, destination)
    }
}
