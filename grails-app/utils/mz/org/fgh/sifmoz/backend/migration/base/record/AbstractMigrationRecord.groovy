package mz.org.fgh.sifmoz.backend.migration.base.record


import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog;
import mz.org.fgh.sifmoz.backend.migrationLog.IMigrationLogService;
import mz.org.fgh.sifmoz.backend.restUtils.RestService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
public abstract class AbstractMigrationRecord implements MigrationRecord {

    protected MigratedRecord migratedRecord;
    protected RestService restService;
    static Logger logger = LogManager.getLogger(AbstractMigrationRecord.class);
    @Autowired
    protected IMigrationLogService migrationLogService;

    public AbstractMigrationRecord() {
        this.migratedRecord = initMigratedRecord();
    }

    @Override
    public void setAsMigratedSuccessfully(RestService restServiceProvider) {
        logger.info("Registo migrado com sucesso: origem [" + this.getEntityName() + " : " + this.getId() + "] - destino: [" + this.migratedRecord.getEntity() + " : " + this.migratedRecord.getId() + "]");
        String uri = "/" + this.getEntityName().toLowerCase() + "?"+getIdFieldName()+"=eq." + getId();
        restServiceProvider.patch(uri, "{\"migration_status\":\"MIGRATED\"}");
        String[] msg = ["Registo Migrado com Sucesso"]
        MigrationLog log = new MigrationLog("UNKNOWN", msg, getId(), getEntityName(), this.migratedRecord.getId(), this.migratedRecord.getEntity(), "MIGRATED")
        MigrationLog.withTransaction {
            log.save(flush: true, failOnError: true);
        }
    }

    @Override
    public List<MigrationLog> generateUnknowMigrationLog(MigrationRecord record, String message) {
        List<MigrationLog> migrationLogs = new ArrayList<>();
        String[] msg = [message]
        migrationLogs.add(new MigrationLog("UNKNOWN", msg, record.getId(), record.getEntityName()));
        return migrationLogs;
    }

    @Override
    public void setAsRejectedForMigration(RestService restServiceProvider) {
        logger.info("Registo não migrado: [" + this.getEntityName() + " : " + this.getId() + "]");
        String uri = "/" + this.getEntityName().toLowerCase() + "?id=eq." + getId();
        restServiceProvider.patch(uri, "{\"migration_status\":\"REJECTED\"}");
    }

    public MigratedRecord getMigratedRecord() {
        return migratedRecord;
    }

    @Override
    public void saveMigrationLogs(List<MigrationLog> migrationLogs) {
        MigrationLog.withTransaction {
            for (MigrationLog migrationLog : migrationLogs) {
                migrationLog.save(flush: true, failOnError: true);
            }
        }
    }

    @Override
    void deletePreviousLogs() {
        MigrationLog.withTransaction {
            List<MigrationLog> logs = MigrationLog.findAllBySourceIdAndSourceEntity(getId(), getEntityName())
            MigrationLog.deleteAll(logs)
        }
    }

    @Override
    public void setMigratedRecord(MigratedRecord migratedRecord) {
        this.migratedRecord= migratedRecord;
    }

    @Override
    RestService getRestService() {
        return this.restService;
    }

    @Override
    void setRestService(RestService restService) {
        this.restService = restService;
    }

    @Override
    String getIdFieldName() {
        return "id"
    }
}
