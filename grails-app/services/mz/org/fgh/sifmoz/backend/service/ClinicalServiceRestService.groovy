package mz.org.fgh.sifmoz.backend.service

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import org.grails.web.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Transactional
class ClinicalServiceRestService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetClinicalServiceFromProvincialServer"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("GetClinicalServiceFromProvincialServer");

    static lazyInit = false

    @Transactional
    List<ClinicalService> loadClinicalServiceFromProvincial(int offset) {
        ClinicalService.withTransaction {
            LOGGER.info("Iniciando a Busca de Servicos Clinicos no Servidor Provincial")
            def clinicalServiceList = new ArrayList<ClinicalService>()

            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinic = Clinic.findWhere(id: this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findWhere(code: clinic.getProvince().code, destination: IDMED_SERVER)

                String urlPath = "/api/clinicalService?offset=" + offset + "&max=100"
                def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray

                for (def clinicalServiceObject : response) {
                    try {
                        def clinicalServiceExist = ClinicalService.findWhere(id: clinicalServiceObject.getAt("id"))
                        if (!clinicalServiceExist && clinicalServiceObject.getAt("active")){
                            clinicalServiceExist = new ClinicalService()
                            clinicalServiceExist.id = clinicalServiceObject.getAt(("id"))
                            clinicalServiceExist.code = clinicalServiceObject.getAt(("code"))
                            clinicalServiceExist.description = clinicalServiceObject.getAt(("description"))
                            clinicalServiceExist.identifierType = IdentifierType.findById(clinicalServiceObject.getAt(("identifierTypeId")) as String)
                            clinicalServiceExist.active = clinicalServiceObject.getAt(("active"))
                            clinicalServiceExist.save()
                        }
                        if(clinicalServiceExist)
                            clinicalServiceList.add(clinicalServiceExist)
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
            return clinicalServiceList
        }
    }

    @Override
    void execute() {

    }
}
