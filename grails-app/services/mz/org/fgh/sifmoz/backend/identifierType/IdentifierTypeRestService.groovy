package mz.org.fgh.sifmoz.backend.identifierType

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import org.grails.web.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@Transactional
class IdentifierTypeRestService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetIdentifierTypeFromProvincialServer"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestGetIdentifierTypeFromProvicnialServer");

    static lazyInit = false

//    @Scheduled(cron = "0 10 20 * * *")
    void execute() {
        def offset = 0
        def count = loadIdentifierTypesFromProvincial(offset).size()

        if(count > 0 ){
            offset  = offset + 100
            loadIdentifierTypesFromProvincial(offset)
        }
    }

    @Transactional
    List<IdentifierType> loadIdentifierTypesFromProvincial(int offset) {
        IdentifierType.withTransaction {
            LOGGER.info("Iniciando a Busca de Farmácias no Servidor Provincial")
            def identifierTypeList = new ArrayList<IdentifierType>()

            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinic = Clinic.findWhere(id: this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findWhere(code: clinic.getProvince().code, destination: IDMED_SERVER)

                String urlPath = "/api/identifierType?offset=" + offset + "&max=100"
                def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray

                for (def identifierTypeObject : response) {
                    try {
                        def identifierTypeExist = IdentifierType.findWhere(code: identifierTypeObject.getAt("code"))

                        if (!identifierTypeExist){
                            identifierTypeExist = new IdentifierType()
                            identifierTypeExist.id = identifierTypeObject.getAt(("id"))
                            identifierTypeExist.code = identifierTypeObject.getAt(("code"))
                            identifierTypeExist.description = identifierTypeObject.getAt(("description"))
                            identifierTypeExist.pattern = identifierTypeObject.getAt(("pattern"))
                            identifierTypeExist.save()
                            identifierTypeList.add(identifierTypeExist)
                        }
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
            return identifierTypeList
        }
    }
}