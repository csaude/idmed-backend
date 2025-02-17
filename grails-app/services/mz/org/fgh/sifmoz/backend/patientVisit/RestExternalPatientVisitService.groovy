package mz.org.fgh.sifmoz.backend.patientVisit


import grails.gorm.transactions.Transactional
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.IdmedAuthenticationUtils
import org.springframework.http.HttpStatus
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
@CompileStatic
@EnableScheduling
@Transactional
class RestExternalPatientVisitService {

    final char syncStatusReady = "R"
    final char syncStatusSent = "S"

    IdmedAuthenticationUtils authenticationIdmedUtils = new IdmedAuthenticationUtils()
    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPatientVisitDetailsService patientVisitDetailsService
    ExternalPatientVisitService externalPatientVisitService
    IPackService packService

    static lazyInit = false

//    @Scheduled(cron = "0 0 12 * * 1,5")
    @Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {

        PatientVisit.withTransaction {
            List<ExternalPatientVisit> externalPatientVisitList = ExternalPatientVisit.findAllWhere(sourceProvinceId: configProvincialUUID(), syncStatus: syncStatusReady)

            externalPatientVisitList.each { externalPatientVisit ->
                try {
                    if (!configProvincialUUID().equalsIgnoreCase(externalPatientVisit.targetProvinceId)) {
                        Province province = Province.findWhere(id: externalPatientVisit.targetProvinceId)
                        ProvincialServer provincialServer = ProvincialServer.findWhere(code: province.code, destination: "IDMED")

                        // Envia a visita do paciente para o respectivo Servidor provincial
                        def resultRequest = authenticationIdmedUtils.syncExternalPatientVisit(provincialServer, externalPatientVisit)

                        if (resultRequest == HttpStatus.CREATED.value() || resultRequest == HttpStatus.CONFLICT.value()) {
                            externalPatientVisit.syncStatus = 'S'
                            externalPatientVisit.save(flush: true)
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to sync external patient visit: ${externalPatientVisit.id}", e)
                    e.printStackTrace()
                }
            }
        }

    }

    private static String configProvincialUUID() {
        SystemConfigs systemConfigs = SystemConfigs.findWhere(key: "INSTALATION_TYPE")
        if (systemConfigs && !systemConfigs.value.equalsIgnoreCase("LOCAL")) {
            return systemConfigs.description
        }
        return null
    }

}