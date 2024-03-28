package mz.org.fgh.sifmoz.backend.tansreference


import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.task.SynchronizerTask
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.provincialServer.ProvincialServer
import mz.org.fgh.sifmoz.backend.restUtils.RestProvincialServerMobileClient
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled

//@Transactional
//@EnableScheduling
//@Slf4j
class RestPostPatientCentralMobileService extends SynchronizerTask {

    private static final NAME = "PostPatientCentralMobile"

    @Autowired
    IPatientVisitDetailsService visitDetailsService
    IEpisodeService episodeService
    @Autowired
    IPatientTransReferenceService patientTransReferenceService
    RestProvincialServerMobileClient restProvincialServerClient = new RestProvincialServerMobileClient()

    static lazyInit = false

    private static final Logger LOGGER = LoggerFactory
            .getLogger("RestMobileDataPostPatient");

    private static final String FORMAT_STRING = '| %1$-10s |  %2$-40s|  %3$-30s|';

    private static final String MESSAGE = String.format(
            FORMAT_STRING,
            "Id Episodio",
            "Nome",
            "NID");

    @Scheduled(fixedDelay = 60000L)
    void execute() {
        PatientTransReference.withTransaction {
            if (this.instalationConfig != null && !this.isProvincial()) {
                Clinic clinicLoged = Clinic.findById(this.getUsOrProvince())
                List<PatientTransReferenceType> patientTransReferenceTypes = PatientTransReferenceType.findAllByCodeInList(Arrays.asList('REFERENCIA_FP', 'REFERENCIA_DC'))
                List<PatientTransReference> patientsTransferees = PatientTransReference.findAllBySyncStatusAndOperationTypeInList('P', patientTransReferenceTypes)

                // Alterar para a linha abaixo quando for em Producao
               ProvincialServer provincialServer = ProvincialServer.findByCodeAndDestination(clinicLoged.getProvince().code , MOBILE_SERVER)

                char SyncP = 'P'
                char SyncS = 'S'
                char False = 'F'

                LOGGER.info("Iniciando o Envio de Pacientes")
                LOGGER.info(MESSAGE)

                for (PatientTransReference pt : patientsTransferees) {
                    String message = String.format(FORMAT_STRING,
                            pt.matchId,
                            pt.patient.firstNames,
                            pt.identifier.value)
                    LOGGER.info("Processando" + message);
                    try {

                        SyncTempPatient syncTempPatient = new SyncTempPatient()

                        Patient patient = Patient.get(pt.identifier.patient.id)
                        List<PatientVisit> patientVisitList = PatientVisit.findAllByPatient(patient)
                        PatientVisitDetails lastVisitDetails = PatientVisitDetails.findAllByPatientVisitInList(patientVisitList).last()
                        Prescription lastPrescription = Prescription.get(lastVisitDetails.prescription.id)
                        Pack lastPack = Pack.get(lastVisitDetails.pack.id)

                        syncTempPatient.setId(Integer.parseInt(pt.matchId.toString()))
                        syncTempPatient.setAccountstatus(false)
                        syncTempPatient.setCellphone(pt.identifier.patient.cellphone != null ? pt.identifier.patient.cellphone : '')
                        syncTempPatient.setDateofbirth(pt.identifier.patient.dateOfBirth)


                        // destination passou a ser uuid de fp ou dispensa comunitaria

                        if (pt.operationType.code.equals("REFERENCIA_FP")) {
                            Clinic clinicDestination = Clinic.findByUuid(pt.destination.contains(":") ? pt.destination.substring(pt.destination.indexOf(":") + 1).trim() : pt.destination.trim())
                            syncTempPatient.setClinicname(clinicDestination.clinicName)
                            syncTempPatient.setClinicuuid(clinicDestination.uuid)
                            // syncTempPatient.setClinic(clinicDestination.matchId)
                        } else if (pt.operationType.code.equals("REFERENCIA_DC")) {
                            ClinicSector clinicSectorDestination = ClinicSector.findById(pt.destination.contains(":") ? pt.destination.substring(pt.destination.indexOf(":") + 1).trim() : pt.destination.trim())
                            syncTempPatient.setClinicname(clinicSectorDestination != null ? clinicSectorDestination.description : 'Desconhecido')
                          //  syncTempPatient.setClinicuuid(clinicSectorDestination != null ? clinicSectorDestination.uuid : 'Desconhecido')
                            syncTempPatient.setClinicuuid(pt.destination.contains(":") ? pt.destination.substring(pt.destination.indexOf(":") + 1).trim() : pt.destination.trim())
                        }
                        syncTempPatient.setClinic(0)
                        syncTempPatient.setMainclinic(0)
                        syncTempPatient.setMainclinicname(pt.origin.clinicName)
                        syncTempPatient.setMainclinicuuid(pt.origin.uuid)
                        syncTempPatient.setFirstnames(pt.identifier.patient.firstNames)
                        syncTempPatient.setHomephone(pt.identifier.patient.alternativeCellphone)
                        syncTempPatient.setLastname(pt.identifier.patient.lastNames)
                        syncTempPatient.setProvince(pt.identifier.patient.province.description)
                        syncTempPatient.setPatientid(pt.identifier.value)
                        if (pt.identifier.patient.gender == 'Masculino') {
                            syncTempPatient.setSex("M")
                        } else {
                            syncTempPatient.setSex("F")
                        }
                        syncTempPatient.setSyncstatus(SyncP)
                        syncTempPatient.setWorkphone('')
                        syncTempPatient.setAddress1(pt.identifier.patient.address)
                        syncTempPatient.setAddress2(pt.identifier.patient.addressReference)
                        syncTempPatient.setAddress3('')
                        syncTempPatient.setRace('')
                        syncTempPatient.setUuidopenmrs(pt.identifier.patient.hisUuid)
                        syncTempPatient.setDatainiciotarv(Utilities.dateformatToDDMMMYYYY(pt.identifier.startDate))
                        syncTempPatient.setSyncuuid(UUID.randomUUID().toString())
                        syncTempPatient.setPrescriptiondate(lastPrescription.prescriptionDate)
                        syncTempPatient.setDuration(lastPrescription.duration.weeks)
                        syncTempPatient.setPrescriptionenddate(lastPrescription.expiryDate)
                        syncTempPatient.setRegimenome(lastPrescription.prescriptionDetails[0].therapeuticRegimen.regimenScheme)

                        if (lastPrescription.prescriptionDetails[0].therapeuticLine.description.contains("1")) {
                            syncTempPatient.setLinhanome("1ª Linha")
                        } else if (lastPrescription.prescriptionDetails[0].therapeuticLine.description.contains("2")) {
                            syncTempPatient.setLinhanome("2ª Linha")
                        } else if (lastPrescription.prescriptionDetails[0].therapeuticLine.description.contains("3")) {
                            syncTempPatient.setLinhanome("3ª Linha")
                        }
                        if (lastPrescription.prescriptionDetails[0].dispenseType.code == 'DM') {
                            syncTempPatient.setDispensasemestral(0)
                            syncTempPatient.setDispensatrimestral(0)
                        } else if (lastPrescription.prescriptionDetails[0].dispenseType.code == 'DT') {
                            syncTempPatient.setDispensasemestral(0)
                            syncTempPatient.setDispensatrimestral(1)
                        } else if (lastPrescription.prescriptionDetails[0].dispenseType.code == 'DS') {
                            syncTempPatient.setDispensasemestral(1)
                            syncTempPatient.setDispensatrimestral(0)
                        }
                        syncTempPatient.setPrescriptionid(lastPrescription.prescriptionSeq)
                        syncTempPatient.setPrescricaoespecial('F')
                        syncTempPatient.setMotivocriacaoespecial('')
                        Map<String, Object> pd = new HashMap<String, Object>()
                        ArrayList listPD = new ArrayList()
                        if (!lastPrescription?.getPrescribedDrugs()?.isEmpty()) {

                            for (PrescribedDrug prescribedDrugs : lastPrescription.getPrescribedDrugs()) {
                                pd.put("drugId", prescribedDrugs.getDrug().getId())
                                pd.put("drugcode", prescribedDrugs.getDrug().getFnmCode())
                                pd.put("timesperday", prescribedDrugs.getTimesPerDay())
                                listPD.add(pd);
                            }
                        } else if (!lastPack?.packagedDrugs?.isEmpty()) {
                            for (PackagedDrug packagedDrug : lastPack?.packagedDrugs) {
                                Drug drug = Drug.get(packagedDrug.drug.id)
                                pd.put("drugId", drug.id)
                                pd.put("drugcode", drug.fnmCode)
                                pd.put("timesperday", packagedDrug.getTimesPerDay())
                                listPD.add(pd);
                            }
                        }
                        syncTempPatient.setJsonprescribeddrugs(listPD.toString().replace("[[", "[").replace("]]", "]"));

                        syncTempPatient.setEstadopaciente(pt.patientStatus);
                        syncTempPatient.setExclusaopaciente(false);
                        syncTempPatient.setModified(False)
                        def obj = Utilities.parseToJSON(syncTempPatient)
                        def response = restProvincialServerClient.postRequestProvincialServerClient(provincialServer, "/sync_temp_patients", obj)
                        if (!response.contains("Wrong")) {
                            if (Integer.parseInt(response) == HttpURLConnection.HTTP_CREATED) {
                                // def ptToUpdate = PatientTransReference.findById(pt.id)
                                pt.syncStatus = SyncS
                                patientTransReferenceService.save(pt)
                            } else if (Integer.parseInt(response) == HttpURLConnection.HTTP_CONFLICT) {
                                def newUrlPath = "/sync_temp_patients?id=eq." + syncTempPatient.getId() + "&mainclinicname=eq." + syncTempPatient.getMainclinicname().replaceAll(" ", "%20");
                                def responsePatch = restProvincialServerClient.patchRequestProvincialServerClient(provincialServer, newUrlPath, obj)
                                if (!responsePatch.contains("Wrong")) {
                                    pt.syncStatus = SyncS
                                    patientTransReferenceService.save(pt)
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace()
                    } finally {
                        continue
                    }
                }
            }
            //remove all sent followup patients after 5 days
            removeOldFollowupReference()
        }
    }

    def removeOldFollowupReference(){
        List<PatientTransReference> missingPatientTransReferenceList = PatientTransReference.findAllBySyncStatusAndPatientStatus("S","Faltoso")
        List<PatientTransReference> lostTofollowupPatientTransReferenceList = PatientTransReference.findAllBySyncStatusAndPatientStatus("S","Abandono")

        for(PatientTransReference transReference : missingPatientTransReferenceList){
            try{
                findPatientAndRemove(transReference)
            }catch (Exception e){
                e.printStackTrace()
            }finally {
                continue
            }
        }

        for(PatientTransReference transReference : lostTofollowupPatientTransReferenceList){
            try{
                findPatientAndRemove(transReference)
            }catch (Exception e){
                e.printStackTrace()
            }finally {
                continue
            }
        }
    }

    def findPatientAndRemove(PatientTransReference transReference){
        def daysLeft = ConvertDateUtils.dateDiff(new Date(), transReference.creationDate)
        if(daysLeft >= 5){
            transReference.delete()
        }
    }
}
