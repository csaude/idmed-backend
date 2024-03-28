package mz.org.fgh.sifmoz.backend.clinic

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.form.Form
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import org.grails.web.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@Transactional
class ClinicRestService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetClinicFromProvincialServer"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestGetClinicFromProvicnialServer");

    static lazyInit = false

//    @Scheduled(cron = "0 10 20 * * *")
    void execute() {
        def offset = 0
        def count = loadClinicsFromProvincial(offset).size()

        if(count > 0 ){
            offset  = offset + 100
            loadClinicsFromProvincial(offset)
        }
    }

    @Transactional
    List<Clinic> loadClinicsFromProvincial(int offset) {
        Clinic.withTransaction {
            LOGGER.info("Iniciando a Busca de Farmácias no Servidor Provincial")
            def clinicList = new ArrayList<Clinic>()

            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinic = Clinic.findWhere(id: this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findWhere(code: clinic.getProvince().code, destination: IDMED_SERVER)

                String urlPath = "/api/clinic?offset=" + offset + "&max=100"
                def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray

                for (def clinicObject : response) {
                    try {
                        def clinicExist = Clinic.findWhere(code: clinicObject.getAt("code"))

                        if (!clinicExist && clinicObject.getAt("active")){
                            clinicExist = new Clinic()
                            clinicExist.id = clinicObject.getAt(("id"))
                            clinicExist.code = clinicObject.getAt(("code"))
                            clinicExist.notes = clinicObject.getAt(("notes"))
                            clinicExist.clinicName = clinicObject.getAt(("clinicName"))
                            clinicExist.province = Province.get(clinicObject.getAt("province").getAt("id") as String)
                            clinicExist.district = District.get(clinicObject.getAt("district").getAt("id") as String)
                            clinicExist.facilityType = FacilityType.get(clinicObject.getAt("facilityType").getAt("id") as String)
                            clinicExist.mainClinic = clinicObject.getAt(("mainClinic"))
                            clinicExist.active = clinicObject.getAt(("active"))
                            clinicExist.uuid = clinicObject.getAt(("uuid"))
                            clinicExist.save()
                        }
                        clinicList.add(clinicExist)
                    } catch (Exception e) {
                        e.printStackTrace()
                    }catch (ConnectException e1){
                        e1.printStackTrace()
                    }finally{
                        continue
                    }
                }
                LOGGER.info("Termino do carregamento de Medicamentos no Servidor Provincial")
            }
            return clinicList
        }
    }
}