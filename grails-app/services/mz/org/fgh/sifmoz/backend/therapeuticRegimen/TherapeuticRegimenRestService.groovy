package mz.org.fgh.sifmoz.backend.therapeuticRegimen

import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.drug.Drug
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
class TherapeuticRegimenRestService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetTherapeuticRegimenFromProvincialServer"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestGetTherapeuticRegimenFromProvicnialServer");

    static lazyInit = false

//    @Scheduled(cron = "0 10 20 * * *")
    void execute() {
        def offset = 0
        def count = loadTherapeuticRegimenFromProvincial(offset)

        if(count > 0 ){
            offset  = offset + 100
            loadTherapeuticRegimenFromProvincial(offset).size()
        }
    }

    @Transactional
    List<TherapeuticRegimen> loadTherapeuticRegimenFromProvincial(int offset) {
        TherapeuticRegimen.withTransaction {
            LOGGER.info("Iniciando a Busca de Regimes Terapeuticos no Servidor Provincial")
            def therapeuticRegimenList = new ArrayList<TherapeuticRegimen>()

            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinic = Clinic.findWhere(id: this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findWhere(code: clinic.getProvince().code, destination: IDMED_SERVER)

                String urlPath = "/api/therapeuticRegimen?offset=" + offset + "&max=100"
                def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray

                for (def therapeuticRegimenObject : response) {
                    try {
                        def therapeuticRegimenExist = TherapeuticRegimen.findWhere(code: therapeuticRegimenObject.getAt("code"))

                        if (!therapeuticRegimenExist && therapeuticRegimenObject.getAt("active")){
                            therapeuticRegimenExist = new TherapeuticRegimen()
                            therapeuticRegimenExist.id = therapeuticRegimenObject.getAt(("id"))
                            therapeuticRegimenExist.regimenScheme = therapeuticRegimenObject.getAt(("regimenScheme"))
                            therapeuticRegimenExist.code = therapeuticRegimenObject.getAt(("code"))
                            therapeuticRegimenExist.description = therapeuticRegimenObject.getAt(("description"))
                            therapeuticRegimenExist.openmrsUuid = therapeuticRegimenObject.getAt(("openmrsUuid"))
                            therapeuticRegimenExist.active = therapeuticRegimenObject.getAt(("active"))
                            getRegimenDrugs(therapeuticRegimenObject.getAt(("drugs")) as ArrayList, therapeuticRegimenExist)
                            therapeuticRegimenExist.save()
                        }
                        therapeuticRegimenList.add(therapeuticRegimenExist)
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
            return therapeuticRegimenList
        }
    }

   def getRegimenDrugs(ArrayList drugs, TherapeuticRegimen therapeuticRegimen){
        for (def drugObject : drugs) {
            try {
                def drugExist = Drug.findWhere(fnmCode: drugObject.getAt("fnmCode"))

                if (!drugExist && drugObject.getAt("active")){
                    drugExist = new Drug()
                    drugExist.id = drugObject.getAt(("id"))
                    drugExist.packSize = drugObject.getAt(("packSize")) as int
                    drugExist.name = drugObject.getAt(("name"))
                    drugExist.defaultTreatment = drugObject.getAt(("defaultTreatment")) as double
                    drugExist.defaultTimes = drugObject.getAt(("defaultTimes")) as int
                    drugExist.defaultPeriodTreatment = drugObject.getAt(("defaultPeriodTreatment"))
                    drugExist.fnmCode = drugObject.getAt(("fnmCode"))
                    drugExist.uuidOpenmrs = drugObject.getAt(("uuidOpenmrs"))
                    drugExist.clinicalService = ClinicalService.get(drugObject.getAt("clinicalService").getAt("id")as String)
                    drugExist.form = Form.get(drugObject.getAt("form").getAt("id") as String)
                    drugExist.active = drugObject.getAt(("active"))
                    drugExist.save()
                }
                therapeuticRegimen.addToDrugs(drugExist)
            } catch (Exception e) {
                e.printStackTrace()
            }catch (ConnectException e1){
                e1.printStackTrace()
            }finally{
                continue
            }
        }
    }
}