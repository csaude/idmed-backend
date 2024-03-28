package mz.org.fgh.sifmoz.backend.migration.base.engine


import mz.org.fgh.sifmoz.backend.healthInformationSystem.ISystemConfigsService;
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.migration.stage.MigrationStage
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog;
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.base.record.MigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.base.search.SearchEngine
import mz.org.fgh.sifmoz.backend.utilities.Utilities;
import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
public class MigrationEngineImpl<T extends AbstractMigrationRecord> implements MigrationEngine<T> {

    static Logger logger = LogManager.getLogger(MigrationEngineImpl.class);

    protected SearchEngine<T> searchEngine;
    protected List<T> recordList;
    protected AbstractMigrationSearchParams<T> searchParams;
    private String engineType;
    private SystemConfigs engineConfig;
    @Autowired
    private ISystemConfigsService systemConfigsService;
    private String engineStatus;

    public static final String ENGINE_RUNNING = "PARAMS_MIGRATION_STAGE";
    public static final String ENGINE_SLEEPING = "PARAMS_MIGRATION_STAGE";
    public static final String ENGINE_FINISHED = "PARAMS_MIGRATION_STAGE";
    public static final String ENGINE_STOPED = "PARAMS_MIGRATION_STAGE";

    public static final String PARAMS_MIGRATION_STAGE = "PARAMS_MIGRATION_STAGE";
    public static final String STOCK_MIGRATION_STAGE = "STOCK_MIGRATION_STAGE";
    public static final String PATIENT_MIGRATION_STAGE = "PATIENT_MIGRATION_STAGE";

    public static final String MIGRATION_ON= "ON";
    public static final String MIGRATION_OFF = "OFF";

    public static final String PATIENT_MIGRATION_ENGINE = "PATIENT_MIGRATION_ENGINE";
    public static final String STOCK_MIGRATION_ENGINE = "STOCK_MIGRATION_ENGINE";
    public static final String PARAMS_MIGRATION_ENGINE = "PARAMS_MIGRATION_ENGINE";

    public MigrationEngineImpl(AbstractMigrationSearchParams<T> searchParams, String engineType) {
        this.searchParams = searchParams;
        this.searchEngine = this.initSearchEngine(searchParams);
        this.engineType = engineType;
    }

    public boolean isPatientEngine () {
        return this.engineType == PATIENT_MIGRATION_ENGINE
    }

    public boolean isStockEngine () {
        return this.engineType == STOCK_MIGRATION_ENGINE
    }

    public boolean isParamsEngine () {
        return this.engineType == PARAMS_MIGRATION_ENGINE
    }

    public String getRelatedStage () {
        if (isPatientEngine()) return PATIENT_MIGRATION_STAGE
        else if (isParamsEngine()) return PARAMS_MIGRATION_STAGE
        else if (isStockEngine()) return STOCK_MIGRATION_STAGE
        else return null
    }

    public void doMigration() {
        this.searchRecords();

        if (Utilities.listHasElements((ArrayList<?>) this.recordList)) {
            engineStatus = ENGINE_RUNNING

            for (MigrationRecord record : recordList) {
                logger.info("Iniciando a migração do registo [" + record.getEntityName()+" : "+record.getId()+"]");

                List<MigrationLog> migrationLogs = null;
                try {
                    migrationLogs = record.migrate();
                } catch (Exception e) {
                    e.printStackTrace()
                    migrationLogs = record.generateUnknowMigrationLog(record, e.getMessage());
                } finally {
                    record.deletePreviousLogs()
                    if (Utilities.listHasElements((ArrayList<?>) migrationLogs)) {
                        record.setAsRejectedForMigration(this.searchParams.getRestServiceProvider());
                        record.saveMigrationLogs(migrationLogs);
                    } else {
                        record.setAsMigratedSuccessfully(this.searchParams.getRestServiceProvider());
                        logger.info("Registo migrado com sucesso, Destino no iDMED: [" + record.getMigratedRecord().getEntity()+" : ID gerado: "+record.getMigratedRecord().getId()+"]");
                    }
                }
            }
            while(stopRequested()) {
                engineStatus = ENGINE_SLEEPING
                try {
                    logger.info("Motor de Migração " + engineType + "Desligado");
                    Thread.sleep(10000);
//                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(!stopRequested()) {
                this.doMigration();
            }
        } else {
            while(!isRelatedMigrationStageCompleted()) {
                engineStatus = ENGINE_SLEEPING
                try {
//                    Thread.sleep(5000);
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                this.doMigration()
            }
            engineStatus = ENGINE_FINISHED
        }
    }

    private MigrationStage getCurrStage() {
        MigrationStage stage = null
        MigrationStage.withTransaction {
            stage = MigrationStage.findByCode(getRelatedStage())
        }
        if (stage == null) throw new RuntimeException(getRelatedStage() + " Not found.")

        return stage
    }

    private boolean isRelatedMigrationStageCompleted() {
        return getCurrStage().getValue() == MigrationStage.STAGE_COMPLETED
    }

    public boolean isRunning() {
        return this.engineStatus == ENGINE_RUNNING
    }

    public boolean isFinished () {
        return this.engineStatus == ENGINE_FINISHED
    }

    private SystemConfigs getEngineConfig(){
        for (SystemConfigs configs: this.systemConfigs) {
            if (configs.getKey().equals(this.engineType)) return configs;
        }
        return null;
    }

    private boolean stopRequested() {
        SystemConfigs.withTransaction {
            this.engineConfig = SystemConfigs.findByKey(this.engineType);
        }
        if (engineConfig == null) throw new RuntimeException("Não foram encontradas as configurações desta migração.");
        if (engineConfig.getValue().equals(MIGRATION_OFF)) return true;
        return false;
    }


    public void searchRecords () {
        this.recordList = this.searchEngine.doSearch();
    }

    @Override
    public SearchEngine<T> initSearchEngine(AbstractMigrationSearchParams<T> searchParams) {
        return new SearchEngine<>(searchParams);
    }

    @Override
    public void run() {
        while(stopRequested()) {
            engineStatus = ENGINE_SLEEPING
            try {
                logger.info("Motor de Migração " + engineType + "Desligado");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!stopRequested()) this.doMigration();
    }
}
