package mz.org.fgh.sifmoz.backend.drug

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.form.Form
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import org.grails.web.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

@CompileStatic
@Slf4j
class DrugRestService extends SynchronizerTask {

    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()
    private static final NAME = "GetDrugFromProvincialServer"

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestGetDrugFromProvicnialServer");

    static lazyInit = false

//    @Scheduled(cron = "0 10 20 * * *")
//    @Scheduled(fixedDelay = 60000L)
    void execute() {
        /*
        def offset = 0
        def count = loadDrugsFromProvincial(offset).size()

        if (count > 0) {
            offset = offset + 100
            loadDrugsFromProvincial(offset)
        }
         */
        def offset = 0
        def count

        while (true) {
            count = loadDrugsFromProvincial(offset).size()

            if (count > 0) {
                offset = offset + 100
                print(offset)

            } else {
                break // Exit the loop when count becomes zero
            }
        }
    }

    @Transactional
    List<Drug> loadDrugsFromProvincial(int offset) {
        Drug.withTransaction {
            LOGGER.info("Iniciando a Busca de Medicamentos no Servidor Provincial")
            def drugList = new ArrayList<Drug>()
            ProvincialServer provincialServer = null
            if (this.instalationConfig != null) {
                if (!this.isProvincial()) {
                    Clinic clinic = Clinic.findWhere(id: this.getUsOrProvince())
                    provincialServer = ProvincialServer.findWhere(code: clinic.getProvince().code, destination: IDMED_SERVER)

                    String urlPath = "/api/drug?offset=" + offset + "&max=100"
                    def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray

                    for (def drugObject : response) {
                        try {
                            def drugExist = Drug.findWhere(fnmCode: drugObject.getAt("fnmCode"))

                            if (!drugExist && drugObject.getAt("active")) {
                                drugExist = new Drug()
                                drugExist.id = drugObject.getAt(("id"))
                                drugExist.packSize = drugObject.getAt(("packSize")) as int
                                drugExist.name = drugObject.getAt(("name"))
                                drugExist.defaultTreatment = drugObject.getAt(("defaultTreatment")) as double
                                drugExist.defaultTimes = drugObject.getAt(("defaultTimes")) as int
                                drugExist.defaultPeriodTreatment = drugObject.getAt(("defaultPeriodTreatment"))
                                drugExist.fnmCode = drugObject.getAt(("fnmCode"))
                                drugExist.uuidOpenmrs = drugObject.getAt(("uuidOpenmrs"))
                                drugExist.clinical_service_id = drugObject.getAt("clinicalService").getAt("id")
//                                drugExist.clinicalService = ClinicalService.get(drugObject.getAt("clinicalService").getAt("id") as String)
                                drugExist.form = Form.get(drugObject.getAt("form").getAt("id") as String)
                                drugExist.active = drugObject.getAt(("active"))
                                drugExist.save()
                            }
                            drugList.add(drugExist)
                        } catch (Exception e) {
                            e.printStackTrace()
                        } catch (ConnectException e1) {
                            e1.printStackTrace()
                        } finally {
                            continue
                        }
                    }
                    LOGGER.info("Termino do carregamento de Medicamentos no Servidor Provincial")

                } else {
                    //  provincialServer = ProvincialServer.findWhere(destination: "METADATA", code: '99')
                    def regimeList = new ArrayList()
                    String urlPath = "/api/product?offset=" + offset + "&max=100"
                    provincialServer = ProvincialServer.findWhere(destination: "METADATA", code: "99")
                    def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath) as JSONArray
                    int i = 0
                    drugList = response
                    for (def drugObject : response) {
                        try {
                            if (drugObject.getAt("status").equals("Activo")) {
                                // Check wheather the drug as TARV OR TB Regimen

                                regimeList = drugObject.getAt("therapeuticRegimenList") as ArrayList
                                if (!regimeList.isEmpty()) {
                                    for (def regimeTerapeutico in regimeList) {
                                        println(regimeTerapeutico.getAt("areaCode"))
                                        if (regimeTerapeutico.getAt("areaCode").equals("T") || regimeTerapeutico.getAt("areaCode").equals("TB")) {
                                            def drugExist = Drug.findWhere(fnmCode: drugObject.getAt("fnm"))
                                            if (!drugExist) {
                                                println(i++)
                                                drugExist = saveDrug(drugExist, drugObject, regimeTerapeutico)
                                            }
                                            // drugList.add(drugExist)
                                            def regimeExist = TherapeuticRegimen.findWhere(code: regimeTerapeutico.getAt("code"))
                                            if (!regimeExist) {
                                                regimeExist = new TherapeuticRegimen()
                                                regimeExist.id = regimeTerapeutico.getAt("id")
                                                regimeExist.regimenScheme = regimeTerapeutico.getAt("description")
                                                regimeExist.active = regimeTerapeutico.getAt("status").equals("Activo")
                                                regimeExist.code = regimeTerapeutico.getAt("code")
                                                regimeExist.description = regimeTerapeutico.getAt("description")
                                                regimeExist.openmrsUuid =  regimeTerapeutico.getAt("uuidOpenmrs")
//                                                regimeExist.clinicalService = regimeTerapeutico.getAt("areaCode").equals("TB") ? ClinicalService.findWhere(code: "TB") : ClinicalService.findWhere(code: "TARV")
                                                regimeExist.beforeInsert()
                                            }
                                            regimeExist.addToDrugs(drugExist)
                                            regimeExist.save(flush:true)
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace()
                        } catch (ConnectException e1) {
                            e1.printStackTrace()
                        } finally {
                            continue
                        }
                    }
                    LOGGER.info("Termino do carregamento de Medicamentos no Servidor Provincial")
                }
            }
            return drugList
        }
    }

    Drug saveDrug(Drug drugExist, def drugObject, def regimeTerapeutico){
        try {
            drugExist = new Drug()
            drugExist.id = drugObject.getAt("id")
            drugExist.packSize = drugObject.getAt("unitsPerPack") as int
            drugExist.name = drugObject.getAt("fullDescription")
            drugExist.defaultTreatment = 1.0 as double
            drugExist.defaultTimes = 1 as int
            drugExist.defaultPeriodTreatment = regimeTerapeutico.getAt("areaCode").equals("TB") ? "": "Dia"
            drugExist.fnmCode = drugObject.getAt("fnm")
            drugExist.uuidOpenmrs = drugObject.getAt("uuidOpenmrs")
            drugExist.clinical_service_id = regimeTerapeutico.getAt("areaCode").equals("TB") ? ClinicalService.findWhere(code: "TB").id : ClinicalService.findWhere(code: "TARV").id
//            drugExist.clinicalService = regimeTerapeutico.getAt("areaCode").equals("TB") ? ClinicalService.findWhere(code: "TB") : ClinicalService.findWhere(code: "TARV")
            drugExist.form = findOrSave(drugObject.getAt("pharmaceuticFormDescription") as String)
            drugExist.active = true
            drugExist.beforeInsert()
            drugExist.validate()
            drugExist.save(flush:true)
        }catch (Exception e){
            e.printStackTrace()
        }
        return drugExist
    }

    Form findOrSave(String formDescription){
        def serachParam = formDescription.substring(0, Math.min(formDescription.size(), 4)).trim()

        if(serachParam.equalsIgnoreCase("Emba"))
            serachParam = "Comp"

        List<Form> forms = Form.createCriteria().list {
            ilike('description', "%${serachParam}%")
        } as List<Form>

        def form = forms.size() == 0 ? new Form() : forms.get(0)
        if(form.id == null){
           // form = new Form()
            form.beforeInsert()
            form.code = serachParam
            form.description = formDescription
            form.save()
        }

        return form
    }

    @Transactional
    List<Drug> getDrugsByInventoryId(String id) {
        List<Drug> list = Drug.executeQuery(" select d from StockAdjustment adj inner join  " +
                "adj.adjustedStock st  inner join st.drug d " +
                "where  adj.inventory.id =: id " +
                "group by d.fnmCode, d.name,d.id",
                [id: id]);
        return list
    }

}