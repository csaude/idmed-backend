package mz.org.fgh.sifmoz.backend.migrationLog

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional

@Transactional
@Service(MigrationLog)
abstract class MigrationLogService implements IMigrationLogService {

    @Override
    MigrationLog get(Serializable id) {
        return null
    }

    @Override
    List<MigrationLog> resultList() {
        return MigrationLog.findAll("from MigrationLog as ml where ml.status='REJECTED' order by ml.sourceEntity")
    }

    @Override
    Long count() {
        return null
    }

    @Override
    MigrationLog delete(Serializable id) {
        return null
    }

    @Override
    MigrationLog save(MigrationLog migrationLog) {
        return null
    }
}
