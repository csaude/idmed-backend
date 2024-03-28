package mz.org.fgh.sifmoz.backend.clinicSector

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReference
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.apache.http.HttpResponse
import org.apache.http.entity.StringEntity
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Transactional
class RestClinicSectorService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    IClinicSectorService clinicSectorService

    static lazyInit = false

    @Scheduled(fixedDelay = 30000L)
    void schedulerRequestRunning() {
        ClinicSector.withTransaction {
            if (this.instalationConfig != null &&  !this.isProvincial()) {
                Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
                List<ClinicSector> clinicSectorList = ClinicSector.findAllBySyncStatus('P')

                // Alterar para a linha abaixo quando for em Producao
            ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged?.province?.code , "MOBILE")

                char SyncP = 'P'
                char SyncS = 'S'
                char False = 'F'

                for (ClinicSector clinicSector : clinicSectorList) {
                    try {
                        if(postClinicFirst(clinicSector.clinic, provincialServer)) {

                            def sectorTypeID = 0

                            if (clinicSector.clinicSectorType.code.equalsIgnoreCase(("PARAGEM_UNICA")))
                                sectorTypeID = 1
                            else if (clinicSector.clinicSectorType.code.equalsIgnoreCase(("PROVEDOR")))
                                sectorTypeID = 2
                            else if (clinicSector.clinicSectorType.code.equalsIgnoreCase(("APE")))
                                sectorTypeID = 3
                            else if (clinicSector.clinicSectorType.code.equalsIgnoreCase(("CLINICA_MOVEL")))
                                sectorTypeID = 4
                            if (clinicSector.clinicSectorType.code.equalsIgnoreCase(("BRIGADA_MOVEL")))
                                sectorTypeID = 5

                            String clinicJSONObject = "{\"id\": \"" + getRandomNumber() +
                                    "\", \"code\": \"" + clinicSector.getCode() +
                                    "\", \"sectorname\": \"" + clinicSector.getDescription() +
                                    "\", \"clinicsectortype\":\"" + sectorTypeID +
                                    "\", \"telephone\": \""+
                                    "\", \"clinic\":\"" + clinicSector.getClinic().getUuid() +
                                    "\", \"clinicuuid\":\"" + clinicSector.getClinic().getUuid() +
                                    "\", \"uuid\":\"" + clinicSector.getUuid() + "\"}";

                            StringEntity inputAddDispense = new StringEntity(clinicJSONObject, "UTF-8");
                            inputAddDispense.setContentType("application/json");

                            def response = restProvincialServerClient.postRequestProvincialServerClient(provincialServer, "/clinicsector", inputAddDispense)
                            if (!response.contains("Wrong")) {
                                clinicSector.syncStatus = SyncS
                                clinicSectorService.save(clinicSector)
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        continue
                    }

                }
            }
        }

    }

    def postClinicFirst(Clinic clinic, provincialServer){
        String facilitytype = "Unidade Sanitária"

        Province province = Province.get(clinic.province.id)
        District district = District.get(clinic.district.id)

        String clinicJSONObject = "{\"id\": \"" + getRandomNumber() +
                "\", \"mainclinic\": \"" + false +
                "\", \"notes\": \"" + clinic.getNotes() +
                "\", \"code\":\"" + clinic.getCode() +
                "\", \"telephone\":\"" + clinic.getTelephone() +
                "\", \"facilitytype\":\"" + facilitytype +
                "\", \"clinicname\":\"" + clinic.getClinicName() +
                "\", \"province\":\"" + province?.description +
                "\", \"district\":\"" + district?.description +
                "\", \"subdistrict\":\"-\",\"uuid\":\"" + clinic.getUuid() + "\"}";

        StringEntity inputAddDispense = new StringEntity(clinicJSONObject, "UTF-8");

        inputAddDispense.setContentType("application/json");

        def response = restProvincialServerClient.postRequestProvincialServerClient(provincialServer as ProvincialServer, "/clinic", inputAddDispense)

        return !response.contains("Wrong")

    }

    def getRandomNumber(){
        Random r = new Random()
        int low = 60000000
        int high = 1000000000;
        int genId = r.nextInt(high - low) + low;
        return genId
    }

    @Override
    void execute() {

    }
}
