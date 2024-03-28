package mz.org.fgh.sifmoz.backend.migration.entity.pack

import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.dispenseMode.DispenseMode
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.DateUtils
import org.codehaus.groovy.runtime.DateGroovyMethods

class PackageMigrationRecord extends AbstractMigrationRecord{

    private Integer packid
    private int prescriptionid
    private int episodeid
    private String reasonforpackagereturn
    private boolean packagereturnedpack
    private String modifiedpack
    private Date datereceivedpack
    private boolean stockreturnedpack
    private Date dateleftpack
    private Date pickupdatepack
    private Date packdate
    private Integer weekssupply
    private Date nextpickupdate
    private String modedispenseuuid
    private Integer patientid
    private String clinicuuid
    private String tipodedoenca
    private String episodeStartdate

    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        Pack.withTransaction {
            MigrationLog patientMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.patientid, "Patient")
            MigrationLog prescriptionMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.prescriptionid, "Prescription")
            MigrationLog episodeMigrationLog
            Episode episodeStartDate
            Episode episode
            if (this.episodeid > 0) {
                    episodeMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.episodeid, "Episode")
                if (episodeMigrationLog == null) {
                    if(this.tipodedoenca == "TB" || this.tipodedoenca == "PREP" || this.tipodedoenca == "TARV") {
                        ClinicalService clinicalService = ClinicalService.findByCode(this.tipodedoenca == "TB" ? "TPT" : this.tipodedoenca)
                        PatientServiceIdentifier psiLocal = PatientServiceIdentifier.findByPatientAndService(
                                Patient.findById(patientMigrationLog.iDMEDId), clinicalService)
                        episodeStartDate = Episode.findByPatientServiceIdentifierAndEpisodeTypeAndEpisodeDateNotGreaterThan(psiLocal, EpisodeType.findByCode("INICIO"),ConvertDateUtils.getDateAtEndOfDay(this.pickupdatepack))
                }
                }
            }
            if (patientMigrationLog == null) throw new RuntimeException("MigrationLog of Patient " + this.patientid + " not found.")
            if (prescriptionMigrationLog == null) throw new RuntimeException("MigrationLog of Prescription " + this.prescriptionid + " not found.")
            if (episodeMigrationLog == null && this.episodeid > 0 && episodeStartDate == null) throw new RuntimeException("MigrationLog of Episode " + this.episodeid + " not found.")


            if (episodeMigrationLog != null) {
                if(this.tipodedoenca == "TB" || this.tipodedoenca == "PREP" || this.tipodedoenca == "TARV") {
                    ClinicalService clinicalService = ClinicalService.findByCode(this.tipodedoenca == "TB" ? "TPT" : this.tipodedoenca)
                    PatientServiceIdentifier psiLocal = PatientServiceIdentifier.findByPatientAndService(
                            Patient.findById(patientMigrationLog.iDMEDId),clinicalService)
                    episode = Episode.findByPatientServiceIdentifierAndEpisodeTypeAndEpisodeDateNotGreaterThan(psiLocal, EpisodeType.findByCode("INICIO"), ConvertDateUtils.getDateAtEndOfDay(this.pickupdatepack))
               if (episode == null) {
                   episode = Episode.findById(episodeMigrationLog.getiDMEDId())
                  }
                } else {
                    episode = Episode.findById(episodeMigrationLog.getiDMEDId())
                }
            } else {
                episode = Episode.findByNotes("Episodio criado para prescricoes sem episodio no iDART")
            }
            if (episode == null)  throw new RuntimeException("Nao existe um episodio para associar a dispensa.")

            Prescription prescription = Prescription.findById(prescriptionMigrationLog.getiDMEDId())

            Clinic clinic = Clinic.findByUuid(clinicuuid)
            if (clinic == null) clinic = Clinic.findByMainClinic(true)

            Patient patient = Patient.findById(patientMigrationLog.getiDMEDId())

            Pack pack = getMigratedRecord()

           PatientVisit existingPatientVisit = findPatientVisitByPatientAndDate(this.pickupdatepack, patient)

            if (existingPatientVisit != null) {
                existingPatientVisit.patientVisitDetails.each {item ->
                    if (item.episode.patientServiceIdentifier.service.code == (this.tipodedoenca == "TB" ? "TPT" : this.tipodedoenca)) {
                        throw new RuntimeException("O paciente ja possui um registo de dispensa para este servico de saude na data [" + this.pickupdatepack + "], impossivel gravar esta dispensa.")
                    }
                }
            }

          //  if (isDuplicatedPack(this.pickupdatepack, patient)) throw new RuntimeException("O paciente ja possui um registo de dispensa na data [" + this.pickupdatepack + "], impossivel gravar esta dispensa.")

            pack.setReasonForPackageReturn(this.reasonforpackagereturn)
            pack.setPickupDate(this.pickupdatepack)
            pack.setPackageReturned(this.packagereturnedpack ? 1 : 0)
            pack.setModified(this.modifiedpack == "T")
            pack.setDateReceived(this.datereceivedpack)
            pack.setStockReturned(this.stockreturnedpack ? 1 : 0)
            pack.setDateLeft(this.dateleftpack)
            pack.setWeeksSupply(this.weekssupply)
            pack.setPackDate(this.packdate)
            pack.setPickupDate(this.pickupdatepack)
            pack.setId(UUID.randomUUID().toString())




            PatientVisitDetails patientVisitDetails = new PatientVisitDetails()
            patientVisitDetails.setId(UUID.randomUUID().toString())
            patientVisitDetails.setClinic(clinic)
            patientVisitDetails.setPrescription(prescription)
            patientVisitDetails.setEpisode(episode)

            patientVisitDetails.setPack(pack)



            //Date nxtPickDt = ConvertDateUtils.createDate(StringUtils.replace(this.nextpickupdate, " ", "-"), "dd-MMM-yyyy")
            pack.setNextPickUpDate(this.nextpickupdate)
           Set<PatientVisitDetails> patientVisitDetailsSet = new HashSet<>();
            DispenseMode dsmode = DispenseMode.findById(modedispenseuuid)
            if (existingPatientVisit == null) {
                PatientVisit patientVisit = new PatientVisit()
                patientVisit.setId(UUID.randomUUID().toString())
                patientVisit.setClinic(clinic)
                patientVisit.setVisitDate(this.pickupdatepack)
                patientVisit.setPatient(patient)
                patientVisitDetails.setPatientVisit(patientVisit)
              //  pack.setPatientVisitDetails(patientVisitDetailsSet)
                patientVisit.validate()
                if (!patientVisit.hasErrors()) {
                    patientVisit.save(flush: true)
                } else {
                    logs.addAll(generateUnknowMigrationLog(this, patientVisit.getErrors().toString()))
                    return logs
                }
            } else {
                patientVisitDetails.setPatientVisit(existingPatientVisit)
             //   pack.setPatientVisitDetails(patientVisitDetailsSet)
             //   existingPatientVisit.patientVisitDetails.add(patientVisitDetails)
            }
          //  pack.setPatientVisitDetails(patientVisitDetailsSet)
            patientVisitDetailsSet.add(patientVisitDetails)
            pack.setDispenseMode(dsmode == null ? DispenseMode.findByCode("US_FP_HN") : dsmode)
            pack.setClinic(clinic)
          //  pack.syncStatus = 'S'
            pack.syncStatus = 'N'
            pack.validate()

            if (!pack.hasErrors()) {
                pack.save(flush: true)
                //pack.setPatientVisitDetails(new HashSet<PatientVisitDetails>())
                patientVisitDetails.save(flush: true)
            } else {
                logs.addAll(generateUnknowMigrationLog(this, pack.getErrors().toString()))
                return logs
            }
        }
        return logs
    }

    @Override
    void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        return this.packid
    }

    @Override
    String getEntityName() {
        return "package"
    }

    @Override
    Pack getMigratedRecord() {
        return super.getMigratedRecord() as Pack
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new Pack()
    }

    PatientVisit findPatientVisitByPatientAndDate(Date pickUpDate, Patient patient) {
        PatientVisit patientVisit = PatientVisit.findByPatientAndVisitDateBetween(patient, ConvertDateUtils.getDateAtStartOfDay(pickupdatepack) , ConvertDateUtils.getDateAtEndOfDay(pickUpDate))
        // PatientVisitDetails patientVisitDetails = PatientVisitDetails.findBy
        return patientVisit
    }
}
