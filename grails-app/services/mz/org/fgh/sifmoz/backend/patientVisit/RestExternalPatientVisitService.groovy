package mz.org.fgh.sifmoz.backend.patientVisit

import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Slf4j
@CompileStatic
@EnableScheduling
@Transactional
class RestExternalPatientVisitService {

    final String requestMethod_POST = "POST"
    final char syncStatusReady = "R"

    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPatientVisitDetailsService patientVisitDetailsService
    ExternalPatientVisitService externalPatientVisitService
    IPackService packService

    static lazyInit = false

//    @Scheduled(cron = "0 0 12 * * 1,5")
//    @Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {
        PatientVisit.withTransaction {
            List<ExternalPatientVisit> externalPatientVisitList = ExternalPatientVisit.findAllBySourceProvinceIdAndSyncStatus(configProvincialUUID(), syncStatusReady)

            externalPatientVisitList.each { externalPatientVisit ->

                Province province = Province.findWhere(id: externalPatientVisit.targetProvinceId)
                ProvincialServer provincialServer = ProvincialServer.findWhere(code: province.code, destination: "IDMED")

            }

        }
    }

    private static String configProvincialUUID(){
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if(systemConfigs && !systemConfigs.value.equalsIgnoreCase("LOCAL")){
            return systemConfigs.description
        }

        return null
    }

}