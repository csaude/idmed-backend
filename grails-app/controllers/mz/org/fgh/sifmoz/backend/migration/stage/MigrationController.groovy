package mz.org.fgh.sifmoz.backend.migration.stage

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.healthInformationSystem.ISystemConfigsService
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.migration.base.engine.MigrationEngineImpl
import mz.org.fgh.sifmoz.backend.migration.entity.patient.PatientMigrationRecord
import mz.org.fgh.sifmoz.backend.migrationLog.IMigrationLogService
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.multithread.ExecutorThreadProvider
import org.grails.web.json.JSONArray

import java.util.concurrent.ExecutorService

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class MigrationController extends RestfulController{

    IMigrationLogService migrationLogService
    MigrationStageService migrationStageService
    MigrationEngineImpl migrationEngine
    ISystemConfigsService systemConfigsService
    MigrationService migrationService
    private static ExecutorService executor;
    private List<SystemConfigs> systemConfigs

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    MigrationController() {
        super(MigrationStage.class)
        systemConfigs = SystemConfigs.list()
        executor = ExecutorThreadProvider.getInstance().getExecutorService();
    }


    def printReport() {
        List<MigrationLog> reportObjects = migrationLogService.resultList()

        render reportObjects as JSON
    }

    def migrationStatus() {
        respond migrationService.getMigrationStatus()
    }

    def migrationStatusDetails(String stage) {
        respond migrationService.getMigrationStatusDetails(stage)
    }

    def migrationIsFinished(String stage) {
     render migrationService.getMigrationIsFinished();
    }

    def initMigration() {
        MigrationStage migrationStage = MigrationStage.findByCode(MigrationEngineImpl.PARAMS_MIGRATION_STAGE)
        migrationStage.setValue(MigrationStage.STAGE_IN_PROGRESS)
        migrationStageService.save(migrationStage)
        ExecutorThreadProvider.getInstance().getExecutorService().execute(migrationService)
        render status: OK
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond migrationStageService.list(params), model:[migrationStageCount: migrationStageService.count()]
    }

    def show(String id) {
        respond migrationStageService.get(id)
    }

    def startMigration() {

    }

    def setMigrationCompleted() {
      migrationService.markMigrationCompleted()
        render status: OK
    }

    @Transactional
    def save(MigrationStage migrationStage) {
        migrationStage.beforeInsert()
        if (migrationStage == null) {
            render status: NOT_FOUND
            return
        }
        if (migrationStage.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond migrationStage.errors
            return
        }

        try {
            migrationStageService.save(migrationStage)
        } catch (ValidationException e) {
            respond migrationStage.errors
            return
        }

        respond migrationStage, [status: CREATED, view:"show"]
    }

    @Transactional
    def update(MigrationStage migrationStage) {
        if (migrationStage == null) {
            render status: NOT_FOUND
            return
        }
        if (migrationStage.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond migrationStage.errors
            return
        }

        try {
            migrationStageService.save(migrationStage)
        } catch (ValidationException e) {
            respond migrationStage.errors
            return
        }

        respond migrationStage, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || migrationStageService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }
}