package mz.org.fgh.sifmoz.backend.tansreference

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.dispenseMode.DispenseMode
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.duration.Duration
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.packagedDrug.IPackagedDrugService
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packaging.IPackService
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.IPatientServiceIdentifierService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.IPatientVisitService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.IPrescriptionDetailService
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.prescriptionDrug.IPrescribedDrugService
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.therapeuticLine.TherapeuticLine
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.apache.http.entity.StringEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Transactional
@EnableScheduling
@Slf4j
class RestGetDispensesCentralMobileService extends SynchronizerTask {

    private static final NAME = "PostPatientCentralMobile"
    @Autowired
    IPatientVisitDetailsService visitDetailsService
    IEpisodeService episodeService
    IPatientServiceIdentifierService patientServiceIdentifierService
    IPatientVisitService patientVisitService
    IPrescriptionService prescriptionService
    IPrescriptionDetailService prescriptionDetailService
    IPrescribedDrugService prescribedDrugService
    IPackService packService
    IPackagedDrugService packagedDrugService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestSendMobileDataGetDispense");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Dispensa",
            "Nome",
            "NID");

    static lazyInit = false


//    @Scheduled(fixedDelay = 60000L)
    void execute() {
        GetDispenseFromProvincialServer()
        SyncDispenseToProvincialServer()
    }

    void SyncDispenseToProvincialServer() {
        if (this.instalationConfig != null && !this.isProvincial()) {
            Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
            ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged.getProvince().code, MOBILE_SERVER)

            List<Pack> refDispenses = Pack.findAllByIsreferralAndIsreferalsynced(true, false)

            LOGGER.info("Iniciando o Envio de Dispensas")
            LOGGER.info(MESSAGE)

            for (Pack pack : refDispenses) {

                PatientVisitDetails patientVisitDetails = PatientVisitDetails.findByPack(pack)
                PatientVisit patientVisit = PatientVisit.get(patientVisitDetails.patientVisit.id)
                Patient patient = Patient.get(patientVisit.patient.id)
                Episode episode = Episode.get(patientVisitDetails.episode.id)
                PatientServiceIdentifier identifier = PatientServiceIdentifier.get(episode.patientServiceIdentifier.id)
                Prescription prescription = Prescription.get(patientVisitDetails.prescription.id)
                PrescriptionDetail prescriptionDetail = PrescriptionDetail.findByPrescription(prescription)
                ClinicalService service = ClinicalService.get(identifier.service.id)
                Clinic clinic = Clinic.get(patient.clinic.id)
                List<PatientTransReference> patientTransReference = PatientTransReference?.findAllByPatient(patient)

                String message = String.format(FORMAT_STRING,
                        patient.matchId,
                        patient.firstNames.concat(" ".concat(patient.lastNames)),
                        identifier.value)

                LOGGER.info("Processando" + message)
                try {
                    if (!patientTransReference.isEmpty()) {
                        def syncTempDipsnenseJSONObject = createSyncTempDispenseObject(pack, patient, identifier, prescription, prescriptionDetail, service, clinic, patientTransReference?.last())

                        def response = restProvincialServerClient.postRequestProvincialServerClient(provincialServer, "/sync_temp_dispense", syncTempDipsnenseJSONObject)

                        if (Integer.parseInt(response) == HttpURLConnection.HTTP_CREATED) {
                            pack.isreferalsynced = true
                            pack.save(flush: true)
                        }
                    } else {
                        LOGGER.info("O Paciente nao possui referencia " + message)
                    }
                } catch (Exception e) {
                    e.printStackTrace()
                } finally {
                    continue
                }
            }
        }
    }

    StringEntity createSyncTempDispenseObject(Pack pack, Patient patient,
                                              PatientServiceIdentifier identifier,
                                              Prescription prescription,
                                              PrescriptionDetail prescriptionDetail,
                                              ClinicalService service, Clinic clinic, PatientTransReference patientTransReference) {
        Random r = new Random()
        StringEntity inputAddPatient
        def packagedDrug = pack.packagedDrugs.first()
        def drugType = service.code.contains("ARV") ? "TARV" : "TPT"
        def motivoMudancaTerapeutica = prescriptionDetail.spetialPrescriptionMotive != null ? prescriptionDetail.spetialPrescriptionMotive.description : ''
        def dt = prescriptionDetail.dispenseType.DT ? 1 : 0
        def tipoDT = prescriptionDetail.dispenseType.DT ? "Manuntecao" : ""
        def tipoDS = prescriptionDetail.dispenseType.DS ? "Manuntecao" : ""
        def ds = prescriptionDetail.dispenseType.DS ? 1 : 0
        def precricaoEspecial = prescriptionDetail.spetialPrescriptionMotive != null ? "T" : "F"
        def motivoPrescricaoEspecial = prescriptionDetail.spetialPrescriptionMotive != null ? prescriptionDetail.spetialPrescriptionMotive.description : ""
        def clinicUUID = patientTransReference.destination.contains(":") ? patientTransReference.destination.substring(patientTransReference.destination.indexOf(":") + 1).trim() : patientTransReference.destination
        def dataValidadePrescricao = prescription.expiryDate == null ? "  \"enddate\": " + null + ",\n" : "  \"enddate\": \"" + prescription.expiryDate + "\",\n"
        def dataExpiraPrescricao = prescription.expiryDate == null ? "  \"expirydate\": " + null + ",\n" : "  \"expirydate\": \"" + prescription.expiryDate + "\",\n"
        def prescriptionFromat =
                "{\n" +
                        "  \"id\": " + Integer.parseInt(patient.matchId.toString().concat((r.nextInt(100) + 1) as String)) + ",\n" +
                        "  \"clinicalstage\": 0,\n" +
                        "  \"current\": \"F\",\n" +
                        "  \"date\": \"" + prescription.prescriptionDate + "\",\n" +
                        "  \"doctor\": 0,\n" +
                        "  \"duration\": " + prescription.duration.weeks + ",\n" +
                        "  \"modified\": \"T\",\n" +
                        "  \"patient\": " + patient.matchId + ",\n" +
                        "  \"weight\": 0,\n" +
                        "  \"reasonforupdate\": \"Manter\",\n" +
                        "  \"notes\": \"" + prescription.notes + "\",\n" + dataValidadePrescricao +
                        "  \"drugtypes\": \"" + drugType + "\",\n" +
                        "  \"regimenome\": \"" + prescriptionDetail.therapeuticRegimen.regimenScheme + "\",\n" +
                        "  \"datainicionoutroservico\": " + null + ",\n" +
                        "  \"motivomudanca\": \"" + motivoMudancaTerapeutica + "\",\n" +
                        "  \"linhanome\": \"" + prescriptionDetail.therapeuticLine.description + "\",\n" +
                        "  \"dispensatrimestral\": " + dt + ",\n" +
                        "  \"tipodt\": \"" + tipoDT + "\",\n" +
                        "  \"ppe\": \"F\",\n" +
                        "  \"ptv\": \"F\",\n" +
                        "  \"tb\": \"F\",\n" +
                        "  \"tpi\": \"F\",\n" +
                        "  \"tpc\": \"F\",\n" +
                        "  \"gaac\": \"F\",\n" +
                        "  \"af\": \"F\",\n" +
                        "  \"ca\": \"F\",\n" +
                        "  \"ccr\": \"F\",\n" +
                        "  \"saaj\": \"F\",\n" +
                        "  \"fr\": \"F\",\n"

        def dispense =
                "  \"weekssupply\": " + pack.weeksSupply + ",\n" +
                        "  \"dispensedate\": \"" + pack.pickupDate + "\",\n" +
                        "  \"amountpertime\": \"" + packagedDrug.amtPerTime + "\",\n" +
                        "  \"drugname\": \"" + packagedDrug.drug.name + "\",\n" +
                        "  \"timesperday\": " + packagedDrug.timesPerDay + ",\n" +
                        "  \"qtyinhand\": \"(" + (int) packagedDrug.quantitySupplied + ")\",\n" +
                        "  \"summaryqtyinhand\": \"(" + (int) packagedDrug.quantitySupplied + ")\",\n" +
                        "  \"qtyinlastbatch\": \"(" + (int) packagedDrug.quantitySupplied + ")\",\n" + dataExpiraPrescricao +
                        "  \"patientid\": \"" + identifier.value + "\",\n" +
                        "  \"patientfirstname\": \"" + patient.firstNames + "\",\n" +
                        "  \"patientlastname\": \"" + patient.lastNames + "\",\n" +
                        "  \"dateexpectedstring\": \"" + Utilities.dateformatToDDMMMYYYY(pack.nextPickUpDate) + "\",\n" +
                        "  \"pickupdate\": \"" + pack.pickupDate + "\",\n" +
                        "  \"syncstatus\": \"N\",\n" +
                        "  \"mainclinic\": 2,\n" +
                        "  \"mainclinicname\": \"" + clinic.clinicName + "\",\n" +
                        "  \"mainclinicuuid\": \"" + clinic.uuid + "\",\n" +
                        "  \"prescriptionid\": \"" + prescription.prescriptionSeq + "\",\n" +
                        "  \"tipods\": \"" + tipoDS + "\",\n" +
                        "  \"dispensasemestral\": " + ds + ",\n" +
                        "  \"durationsentence\": \"\",\n" +
                        "  \"dc\": \"F\",\n" +
                        "  \"prep\": \"F\",\n" +
                        "  \"ce\": \"F\",\n" +
                        "  \"cpn\": \"F\",\n" +
                        "  \"prescricaoespecial\": \"" + precricaoEspecial + "\",\n" +
                        "  \"motivocriacaoespecial\": \"" + motivoPrescricaoEspecial + "\",\n" +
                        "  \"syncuuid\": \"\",\n" +
                        "  \"uuidopenmrs\": \"" + patient.hisUuid + "\",\n" +
                        "  \"clinicuuid\": \"" + clinicUUID + "\",\n" +
                        "  \"username\": \"iDMED\",\n" +
                        "  \"tipodoenca\": \"" + drugType + "\"\n" +
                        "}"
        inputAddPatient = new StringEntity(prescriptionFromat.toString().concat(dispense.toString()), "UTF-8")
        inputAddPatient.setContentType("application/json")
        return inputAddPatient
    }

    void GetDispenseFromProvincialServer() {
        Pack.withTransaction {
            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinic = Clinic.findById(this.getUsOrProvince())
                ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinic.getProvince().code, MOBILE_SERVER)
                String urlPath = "/sync_temp_dispense?mainclinicuuid=eq." + clinic.getUuid() + "&syncstatus=eq.P" + "&order=pickupdate.desc";
                LOGGER.info("Iniciando a Busca de Dispensas")
                def response = restProvincialServerClient.getRequestProvincialServerClient(provincialServer, urlPath)
                LOGGER.info(MESSAGE)
                for (Object dispense : response) {
                    try {
                        if (dispense.getAt('patientid') != null) {
                            PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findByValueAndPrefered(dispense.getAt('patientid').toString(), true)

                            if (dispense.getAt('tipodoenca').toString().equalsIgnoreCase("TPT") || dispense.getAt('tipodoenca').toString().equalsIgnoreCase("TB")) {
                                patientServiceIdentifier = PatientServiceIdentifier.findByValueAndService(dispense.getAt('patientid').toString(), ClinicalService.findByCode('TPT'))
                            } else {
                                if (dispense.getAt('tipodoenca').toString().equalsIgnoreCase("PREP"))
                                    patientServiceIdentifier = PatientServiceIdentifier.findByValueAndService(dispense.getAt('patientid').toString(), ClinicalService.findByCode('PREP'))
                            }

                            def clinicuuid = dispense.getAt('clinicuuid').toString()
                            def dispenseMode = DispenseMode.findByCode('US_FP_FHN')
                            def priviteClinic = Clinic.findByUuid(clinicuuid)

                            if (clinicuuid == null || clinicuuid?.trim()?.isEmpty() || clinicuuid.equalsIgnoreCase("null")) {
                                def lastEpisode = episodeService.getLastWithVisitByIndentifier(patientServiceIdentifier, clinic)

                                if (lastEpisode.referralClinic != null) {
                                    dispenseMode = DispenseMode.findByCode('DD_FP')
                                } else {
                                    def clinisector = ClinicSector.findById(lastEpisode?.clinicSector?.id)
                                    dispenseMode = getDispenseMode(clinisector)
                                }
                            } else {

                                if (priviteClinic)
                                    dispenseMode = DispenseMode.findByCode('DD_FP')
                                else {
                                    def clinicSector = ClinicSector.findByUuid(clinicuuid)
                                    dispenseMode = getDispenseMode(clinicSector)
                                }
                            }

                            if (patientServiceIdentifier != null) {
                                def startStop = StartStopReason.findAllByIsStartReason(true)
                                List<Episode> episodeList = Episode.findAllByPatientServiceIdentifier(patientServiceIdentifier, [sort: 'episodeDate', order: 'desc'])
                                Episode episode = episodeList?.first()
                                def prescriprionDate = ConvertDateUtils.createDate(dispense.getAt("date").toString(), "yyyy-MM-dd")
                                def patientVisitDetailsList = PatientVisitDetails.findAllByEpisode(episode)

                                if (patientVisitDetailsList.isEmpty()) {
                                    episodeList = Episode.findAllByPatientServiceIdentifierAndStartStopReasonInList(patientServiceIdentifier, startStop, [sort: 'episodeDate', order: 'desc'])
                                    episode = episodeList?.isEmpty() ? episode : episodeList?.first()
                                    patientVisitDetailsList = PatientVisitDetails.findAllByEpisode(episode)
                                }

                                def lastPrescriprion = Prescription.findByIdInListAndPrescriptionDate(patientVisitDetailsList.prescription.id, prescriprionDate)
                                if (!lastPrescriprion)
                                    lastPrescriprion = createIdmedPrescription(dispense, patientVisitDetailsList.prescription.id)

                                Pack idmedPack = createIdmedPack(dispense, lastPrescriprion, episode, dispenseMode)
                                createIdmedVisit(dispense, lastPrescriprion, idmedPack, episode, patientServiceIdentifier)

                                if (idmedPack) {
                                    def path = "/sync_temp_dispense?mainclinicuuid=eq." + clinic.getUuid() + "&id=eq." + dispense.getAt("id")
                                    println(path)
                                    String obj = '{"syncstatus":"U"}'
                                    def convertedObj = new StringEntity(obj, "UTF-8");
                                    restProvincialServerClient.patchRequestProvincialServerClient(provincialServer, path, convertedObj)
                                }
                            } else {
                                LOGGER.info("Servico de Saude Nao encontrado Para o paciente com o nid:" + dispense.getAt("patientid").toString());
                            }
                        } else {
                            LOGGER.info("paciente sem o nid")
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

    private DispenseMode getDispenseMode(ClinicSector clinicSector) {

        def dispenseMode = DispenseMode.findByCode('DC_PS')
        if (clinicSector) {
            if (clinicSector?.facilityType?.code?.equalsIgnoreCase("PROVEDOR"))
                dispenseMode = DispenseMode.findByCode('DC_PS')
            if (clinicSector?.facilityType?.code?.equalsIgnoreCase("APE"))
                dispenseMode = DispenseMode.findByCode('DC_APE')
            if (clinicSector?.facilityType?.code?.equalsIgnoreCase("CLINICA_MOVEL"))
                dispenseMode = DispenseMode.findByCode('DC_CM_HN')
            if (clinicSector?.facilityType?.code?.equalsIgnoreCase("BRIGADA_MOVEL"))
                dispenseMode = DispenseMode.findByCode('DC_BM_HN')
        }

        return dispenseMode
    }

    private Prescription createIdmedPrescription(Object dispense, List prescriprionList) {

        def prescriprionDate = ConvertDateUtils.createDate(dispense.getAt("date").toString(), "yyyy-MM-dd")
        def doctor = Doctor.findAllByActive(true)?.first()
        List<Prescription> prescriptionList = Prescription.findAllByIdInList(prescriprionList, [sort: 'prescriptionDate', order: 'desc'])

        Prescription prescription = new Prescription()
        prescription.beforeInsert()
        prescription.setPrescriptionDate(prescriprionDate)
        prescription.setExpiryDate(dispense.getAt("expiryDate") != null ? ConvertDateUtils.createDate(dispense.getAt("expiryDate").toString(), "yyyy-MM-dd") : null)
        prescription.setNotes(dispense.getAt("notes").toString())

        prescription.setPrescriptionSeq(dispense.getAt("prescriptionid").toString())
        prescription.setDoctor(prescriptionList.isEmpty() ? doctor : prescriptionList?.first()?.doctor)
        prescription.setClinic(Clinic.findByUuid(dispense.getAt("mainclinicuuid").toString()))

        prescription.setPatientStatus("Manutenção")

        prescription.setPatientType('N/A')

        prescription.setDuration(Duration.findByWeeks(dispense.getAt('duration').toString() as int))

        PrescriptionDetail prescriptionDetail = new PrescriptionDetail()
        prescriptionDetail.beforeInsert()
        prescriptionDetail.setReasonForUpdate(dispense.getAt("reasonforupdate").toString())
        if (dispense.getAt("dispensatrimestral").toString().contains("1")) {
            prescriptionDetail.setDispenseType(DispenseType.findByCode("DT"))
        } else if (dispense.getAt("dispensasemestral").toString().contains("1")) {
            prescriptionDetail.setDispenseType(DispenseType.findByCode("DS"))
        } else {
            prescriptionDetail.setDispenseType(DispenseType.findByCode("DM"))
        }

        if (dispense.getAt("linhanome").toString().contains("1")) {
            prescriptionDetail.setTherapeuticLine(TherapeuticLine.findByCode("1"));
        } else if (dispense.get("linhanome").toString().contains("2")) {
            prescriptionDetail.setTherapeuticLine(TherapeuticLine.findByCode("2"));
        } else if (dispense.get("linhanome").toString().contains("3")) {
            prescriptionDetail.setTherapeuticLine(TherapeuticLine.findByCode("3"));
        }
        prescriptionDetail.setTherapeuticRegimen(TherapeuticRegimen.findByRegimenScheme(dispense.getAt("regimenome").toString()))
        prescriptionDetail.setPrescription(prescription)

        PrescribedDrug prescribedDrug = setPrescribedDrug(dispense, prescription)

        prescription.addToPrescriptionDetails(prescriptionDetail)
        prescription.addToPrescribedDrugs(prescribedDrug)
        prescription.save()
        return prescription
    }

    private Pack createIdmedPack(Object dispense, Prescription prescription, Episode episode, DispenseMode dispenseMode) {
        def pickUpDate = ConvertDateUtils.createDate(dispense.getAt("pickupdate").toString(), "yyyy-MM-dd")
        def patientVisitDetailsList = PatientVisitDetails.findAllByEpisode(episode)
        def lastPack = null

        if (!patientVisitDetailsList.isEmpty()) {
            lastPack = Pack.findByPickupDateAndIdInList(pickUpDate, patientVisitDetailsList.pack.id)
        }

        if (lastPack) {
            addDrugToPack(dispense, lastPack)
            return lastPack
        } else {
            List<Pack> packList = Pack.findAllByIdInList(patientVisitDetailsList.pack.id)
            def providerUuid = ""

            if (packList.isEmpty()) {
                HealthInformationSystem his = HealthInformationSystem.findByAbbreviation("OpenMRS")
                providerUuid = his.interoperabilityAttributes.find { it.interoperabilityType.code == "OPENMRS_USER_PROVIDER_UUID" }.value
            } else {
                providerUuid = packList.first().providerUuid
            }


            Pack dispenseIdmed = new Pack()
            dispenseIdmed.beforeInsert()
            dispenseIdmed.setClinic(prescription.getClinic())
            dispenseIdmed.setModified(false)
            dispenseIdmed.setDateReceived(ConvertDateUtils.createDate(dispense.getAt("pickupdate").toString(), "yyyy-MM-dd"))
            dispenseIdmed.setNextPickUpDate(ConvertDateUtils.getUtilDateFromString(dispense.get("dateexpectedstring").toString(), "dd MMM yyyy"))
            dispenseIdmed.setPickupDate(ConvertDateUtils.createDate(dispense.getAt("pickupdate").toString(), "yyyy-MM-dd"))
            // check
            dispenseIdmed.setDateLeft()
            dispenseIdmed.setPackDate(ConvertDateUtils.createDate(dispense.getAt("pickupdate").toString(), "yyyy-MM-dd"))
            dispenseIdmed.syncStatus = 'R'
            dispenseIdmed.providerUuid = providerUuid
            dispenseIdmed.isreferral = true
            dispenseIdmed.isreferalsynced = true
            dispenseIdmed.setDispenseMode(dispenseMode)
            dispenseIdmed.setWeeksSupply(dispense.getAt("weekssupply") == null || dispense.getAt("weekssupply") == "" ? 0 : Integer.valueOf(dispense.getAt("weekssupply").toString()))
            dispenseIdmed.setStockReturned(0)
            dispenseIdmed.setPackageReturned(0)
            PackagedDrug packagedDrug = setPackagedDrug(dispense, dispenseIdmed)
            dispenseIdmed.addToPackagedDrugs(packagedDrug)
            dispenseIdmed.save()

            return dispenseIdmed
        }


    }

    private PatientVisit createIdmedVisit(Object dispense, Prescription prescription, Pack dispenseIdmed, Episode episode, PatientServiceIdentifier patientServiceIdentifier) {
        def startStop = StartStopReason.findAllByCodeIlike("%REFERIDO_%")
        def pickUpDate = ConvertDateUtils.createDate(dispense.getAt("pickupdate").toString(), "yyyy-MM-dd")
        def patientVisit = PatientVisit.findByVisitDateAndPatient(pickUpDate, patientServiceIdentifier.patient)


        if (!patientVisit) {
            patientVisit = new PatientVisit()
            patientVisit.beforeInsert()
            patientVisit.setClinic(prescription.getClinic())
            patientVisit.setPatient(patientServiceIdentifier.patient)
            patientVisit.setVisitDate(pickUpDate)
        }

        PatientVisitDetails patientVisitDetails = new PatientVisitDetails()
        patientVisitDetails.beforeInsert()
        patientVisitDetails.setPack(dispenseIdmed)
        patientVisitDetails.setPrescription(prescription)
        patientVisitDetails.setEpisode(Episode.findByPatientServiceIdentifierAndStartStopReasonInList(patientServiceIdentifier, startStop))
        patientVisitDetails.setPatientVisit(patientVisit)
        patientVisitDetails.setClinic(prescription.getClinic())
        patientVisitDetails.setEpisode(episode)
        patientVisitDetails.setPatientVisit(patientVisit)

        patientVisit.addToPatientVisitDetails(patientVisitDetails)
        patientVisit.save()
        return patientVisit
    }

    private PrescribedDrug setPrescribedDrug(Object dispense, Prescription prescription) {
        PrescribedDrug prescribedDrug = new PrescribedDrug()
        prescribedDrug.beforeInsert()
        prescribedDrug.setPrescription(prescription)
        prescribedDrug.setDrug(Drug.findByName(dispense.getAt('drugname').toString()))
        prescribedDrug.setModified(false)
        prescribedDrug.setAmtPerTime(dispense.getAt('amountpertime') != null ? Integer.parseInt(dispense.getAt('amountpertime').toString()) : 1)
        prescribedDrug.setForm('Dia')
        prescribedDrug.setTimesPerDay(1)

        //Quantidade Levadae e Prescrita
        String inHand = dispense.get("qtyinhand").toString();

        if (!inHand.isEmpty())
            inHand = inHand.replace('(', ' ').replace(')', ' ').replaceAll("\\s+", "");
        else
            inHand = "0";
        prescribedDrug.setPrescribedQty(Integer.parseInt(inHand))
        return prescribedDrug
    }

    private PackagedDrug setPackagedDrug(Object dispense, Pack dispenseIdmed) {
        //Quantidade Levadae e Prescrita
        String inHand = dispense.get("qtyinhand").toString();

        if (!inHand.isEmpty())
            inHand = inHand.replace('(', ' ').replace(')', ' ').replaceAll("\\s+", "");
        else
            inHand = "0";

        PackagedDrug packagedDrug = new PackagedDrug()
        packagedDrug.beforeInsert()
        packagedDrug.setDrug(Drug.findByName(dispense.getAt('drugname').toString()))
        packagedDrug.setQuantitySupplied(Double.parseDouble(inHand))
        packagedDrug.setPack(dispenseIdmed)
        packagedDrug.setToContinue(true)
        return packagedDrug
    }

    private void addDrugToPrescription(Object dispense, Prescription prescription) {
        PrescribedDrug prescribedDrug = setPrescribedDrug(dispense, prescription)
        prescribedDrugService.save(prescribedDrug)
    }

    private void addDrugToPack(Object dispense, Pack dispenseIdmed) {
        PackagedDrug packagedDrug = setPackagedDrug(dispense, dispenseIdmed)
        packagedDrugService.save(packagedDrug)
    }

}
