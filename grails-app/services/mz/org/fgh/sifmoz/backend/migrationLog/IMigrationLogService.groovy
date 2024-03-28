package mz.org.fgh.sifmoz.backend.migrationLog


interface IMigrationLogService {

    MigrationLog get(Serializable id)

    List<MigrationLog> resultList()

    Long count()

    MigrationLog delete(Serializable id)

    MigrationLog save(MigrationLog migrationLog)

}
