package mz.org.fgh.sifmoz.backend.migration.stage

import grails.gorm.services.Service

@Service(MigrationStage)
interface MigrationStageService {

    MigrationStage get(Serializable id)

    List<MigrationStage> list(Map args)

    Long count()

    MigrationStage delete(Serializable id)

    MigrationStage save(MigrationStage migrationStage)

}
