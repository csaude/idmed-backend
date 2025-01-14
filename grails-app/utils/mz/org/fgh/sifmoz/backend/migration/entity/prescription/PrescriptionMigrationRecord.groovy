package mz.org.fgh.sifmoz.backend.migration.entity.prescription

import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.duration.Duration
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import mz.org.fgh.sifmoz.backend.therapeuticLine.TherapeuticLine
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.apache.commons.lang3.StringUtils

import java.text.SimpleDateFormat


class PrescriptionMigrationRecord extends AbstractMigrationRecord {

    private Integer id
    private Date prescriptiondate
    private String firstnamedoctor
    private Integer patientid
    private String nid;
    private String lastnamedoctor
    private int durationprescription
    private char modifiedprescription
    private Date enddateprescription
    private int dispensatrimestral
    private int dispensasemestral
    private String tipodoenca
    private char tb
    private char saaj
    private char cpn
    private char ccr
    private String clinicuuid
    private String uuidopenmrs
    private Date startdate
    private Date stopdate
    private String startnotes
    private String stopnotes
    private String startreason
    private String stopreason
    private String notesprescription
    private char currentprescrtiption
    private String therapeuticlinecode
    private String therapeuticregimencode
    private String reasonforupdate
    private String motivomudanca //adcionar na viewPrescription
    private Integer episodeid
    private String prescriptionseq


    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        Prescription.withTransaction {
            MigrationLog patientMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.patientid, "Patient")
            if (patientMigrationLog == null) throw new RuntimeException("MigrationLog of Patient " + this.patientid + " not found.")
            ClinicalService clinicalService = ClinicalService.findByCode(this.tipodoenca == "TB" ? "TPT" : this.tipodoenca)
            Clinic clinic = Clinic.findByMainClinic(true)
            Patient patient = Patient.findById(patientMigrationLog.getiDMEDId())
            PatientServiceIdentifier psi = PatientServiceIdentifier.findByPatientAndService(patient, clinicalService)

            if (psi == null) {
                psi = new PatientServiceIdentifier()
                psi.setStartDate(this.startdate)
                psi.setPatient(patient);
                psi.setClinic(clinic)
                psi.setService(clinicalService)
                psi.setIdentifierType(clinicalService.getIdentifierType())
                psi.setValue(this.nid)
                psi.setState("Activo")
                psi.setPrefered(clinicalService.isTARV())
                psi.setValue(this.nid)

                List<PatientServiceIdentifier> listPSI = PatientServiceIdentifier.findAllByPatient(psi.getPatient())

                if (!Utilities.listHasElements(listPSI)) {
                    psi.setPrefered(true)
                } else if (psi.isPrefered()) {
                    for (PatientServiceIdentifier psiObj : listPSI) {
                        psiObj.setPrefered(false)
                        psiObj.setValue(psi.getValue())
                        psiObj.setState("Activo")
                        psiObj.validate()
                        psiObj.save(flush: true)
                    }
                }
            } else {
                psi.setEpisodes(Episode.findAllByPatientServiceIdentifier(psi) as Set<Episode>)
            }
            Prescription prescription = getMigratedRecord()
            PrescriptionDetail prescriptionDetail = new PrescriptionDetail()
            //  Episode newEpisode
            Episode episode
            prescription.setPatientType("N/A")
            if (!psi.hasEpisodes() && this.episodeid > 0) {
                if (this.reasonforupdate == "Alterar") { // Novo Paciente - Epsiodio
                    prescriptionDetail.reasonForUpdate = this.motivomudanca
                    prescription.patientType = "Alterar"
                    episode = createEpisodeFromMigrationData(clinic, psi)
                } else if (this.reasonforupdate.contains('Inic')) {
                    prescription.patientType = "Inicio"
                    episode = createEpisodeFromMigrationData(clinic, psi)
                } else if (this.reasonforupdate.contains('Man')) {
                    prescription.patientType = "Manutenção"
                    episode = createEpisodeFromMigrationData(clinic, psi)
                } else if (this.reasonforupdate == "Transito") { // Quando for isso criar episodio de transito
                    prescription.patientType = "Transito"
                    episode = this.createEpisodeByStartStopReason(psi, StartStopReason.TRANSITO, 'INICIO')
                } else if (this.reasonforupdate == "Transfer de") {
                    prescription.patientType = "Transfer de"
                    episode = this.createEpisodeByStartStopReason(psi, StartStopReason.TRANSFERIDO_DE, 'INICIO')
                } else if (this.reasonforupdate == "Re-Inicio" || this.reasonforupdate == "Reiniciar") {
                    // Continua e Manter - Criar episodio de Reinicio se o paciente ja tiver nao fazer nada
                    prescription.patientType = this.reasonforupdate
                    episode = this.createEpisodeByStartStopReason(psi, StartStopReason.REINICIO_TRATAMETO, 'INICIO')
                } else if (this.reasonforupdate == "Fim (F)") {
                    // Continua e Manter - Criar episodio de Reinicio se o paciente ja tiver nao fazer nada
                    prescription.patientType = this.reasonforupdate
                    episode = this.createEpisodeByStartStopReason(psi, StartStopReason.TERMINO_DO_TRATAMENTO, 'FIM')
                } else {
                    episode = createEpisodeFromMigrationData(clinic, psi)
                }
            } else {
                MigrationLog episodeMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.episodeid, "Episode")
                if (episodeMigrationLog != null) {
                    episode = Episode.findById(episodeMigrationLog.getiDMEDId())
                } else if (episode == null && this.episodeid > 0) {
                    episode = createEpisodeFromMigrationData(clinic, psi)
                } else if (this.episodeid <= 0) {
                    episode = Episode.findByNotes("Episodio criado para prescricoes sem episodio no iDART")
                    if (episode == null) {
                        episode = generateGenericEpisode(clinic, psi)
                    } else {
                        if (episode.getEpisodeDate() < this.prescriptiondate) {
                            episode.setEpisodeDate(this.prescriptiondate)
                            episode.save(flush: true)
                        }
                    }
                }
            }

            EpisodeMigrationRecord episodeMigrationRecord = createEpisodeMigrationRecord(episode)


            prescription.setClinic(clinic)

            Doctor doctor = Doctor.findByFirstnamesIlikeAndLastnameIlike("%Generic%", "%Provider%")
            if (doctor == null) doctor = Doctor.findByFirstnamesIlikeAndLastnameIlike("%Provedor%", "%Desconhecido%")
            if (doctor == null) doctor = Doctor.findByFirstnamesIlikeAndLastnameIlike("%Clinico%", "%Generico%")
            prescription.setDoctor(doctor)

            prescription.setModified(this.modifiedprescription == 'T')
            prescription.setExpiryDate(this.enddateprescription)
            prescription.setCurrent(this.currentprescrtiption == 'T')
            prescription.setDuration(Duration.findByWeeks(this.durationprescription))
            if (prescription.duration == null) {
                DispenseType dispenseType = DispenseType.findByCode(getTipoDispensa())
                if (dispenseType.isDM()) {
                    prescription.setDuration(Duration.findByWeeks(4))
                } else if (dispenseType.isDT()) {
                    prescription.setDuration(Duration.findByWeeks(12))
                } else if (dispenseType.isDS()) {
                    prescription.setDuration(Duration.findByWeeks(24))
                } else prescription.setDuration(Duration.findByWeeks(4))
                prescription.setNotes("A duracao da prescricao foi atribuida devido a problemas de qualidade de dados")
            } else {
                prescription.setNotes(this.notesprescription)
            }
            prescription.setPrescriptionDate(this.prescriptiondate)

            //    prescription.setPatientType(patientType)
            prescription.setPatientStatus("")
            if (this.prescriptionseq != null)
                prescription.setPrescriptionSeq(this.prescriptionseq)
            else
                prescription.setPrescriptionSeq(getNewPrescriptionId(psi, this.prescriptiondate))

            prescriptionDetail.setPrescription(prescription)
            TherapeuticLine therapeuticLine = getLinhaTerapeutica(this.therapeuticlinecode)
            prescriptionDetail.setTherapeuticLine(therapeuticLine)
            TherapeuticRegimen regimen = TherapeuticRegimen.findByCode(StringUtils.trim(therapeuticregimencode))
            prescriptionDetail.setTherapeuticRegimen(regimen)
            prescriptionDetail.setDispenseType(DispenseType.findByCode(getTipoDispensa()))
            prescriptionDetail.setId(UUID.randomUUID().toString())
            prescriptionDetail.setClinic(clinic)
            prescription.setPrescriptionDetails(new ArrayList<>() as Set<PrescriptionDetail>)
            prescription.getPrescriptionDetails().add(prescriptionDetail)
            prescription.setClinic(clinic)

            /*
            if (this.reasonforupdate == "Alterar") { // Novo Paciente - Epsiodio
                prescriptionDetail.reasonForUpdate = this.motivomudanca
                prescription.changeLine = "Alterar"
            } else if (this.reasonforupdate == "Transito") { // Quando for isso criar episodio de transito
                newEpisode = this.createEpisodeByStartStopReason(psi , StartStopReason.TRANSITO,'INICIO')
            } else if (this.reasonforupdate == "Transfer de") {
                newEpisode = this.createEpisodeByStartStopReason(psi , StartStopReason.TRANSFERIDO_DE, 'INICIO')
            } else if (this.reasonforupdate == "Re-Inicio" || this.reasonforupdate == "Reiniciar") { // Continua e Manter - Criar episodio de Reinicio se o paciente ja tiver nao fazer nada
                newEpisode = this.createEpisodeByStartStopReason(psi , StartStopReason.REINICIO_TRATAMETO,'INICIO')
            } else if (this.reasonforupdate == "Fim (F)") { // Continua e Manter - Criar episodio de Reinicio se o paciente ja tiver nao fazer nada
                newEpisode = this.createEpisodeByStartStopReason(psi , StartStopReason.TERMINO_DO_TRATAMENTO,'FIM')
            }
*/
            prescription.validate()

            if (!Utilities.stringHasValue(psi.id)) {
                psi.validate()
                if (!psi.hasErrors()) {
                    if (psi.id == null) psi.setId(UUID.randomUUID().toString())
                    psi.save(flush: true)
                } else {
                    logs.addAll(generateUnknowMigrationLog(this, psi.getErrors().toString()))
                    return logs
                }
            }
            if (!Utilities.stringHasValue(episode.id)) {
                episode.validate()
                if (!episode.hasErrors()) {
                    if (episode.id == null) episode.setId(UUID.randomUUID().toString())
                    episode.save(flush: true)
                    if (this.episodeid > 0) episodeMigrationRecord.setAsMigratedSuccessfully(getRestService())
                } else {
                    logs.addAll(generateUnknowMigrationLog(this, episode.getErrors().toString()))
                    return logs
                }
            }
            if (!prescription.hasErrors()) {
                try {
                    if (prescription.id == null) prescription.setId(UUID.randomUUID().toString())
                    prescription.save(flush: true)
                } catch (Exception e) {
                    e.printStackTrace()
                    throw e
                }
            } else {
                logs.addAll(generateUnknowMigrationLog(this, prescription.getErrors().toString()))
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
        return this.id
    }

    @Override
    String getEntityName() {
        return "Prescription"
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new Prescription()
    }

    @Override
    Prescription getMigratedRecord() {
        return (Prescription) super.getMigratedRecord()
    }


    String getClinicSectorCode() {
        if (this.cpn == 'T') return "CPN"
        if (this.tb == 'T') return "TB"
        if (this.ccr == 'T') return "CCR"
        if (this.saaj == 'T') return "SAAJ"
        return "NORMAL"
    }

    String getTipoDispensa() {
        if (this.dispensasemestral == 1 && this.dispensatrimestral == 1) return "DM"
        else if (this.dispensasemestral == 1) return "DS"
        else if (this.dispensatrimestral == 1) return "DT"
        return "DM"
    }

    TherapeuticLine getLinhaTerapeutica(String linhaTerap) {

        if (linhaTerap.toString().contains("1")) {
            return TherapeuticLine.findByCode("1")
        } else if (linhaTerap.toString().contains("2")) {
            return TherapeuticLine.findByCode("2")
        } else if (linhaTerap.toString().contains("3")) {
            return TherapeuticLine.findByCode("3")
        } else if (linhaTerap.toString().contains("ALT")) {
            return TherapeuticLine.findByCode("1_ALT")
        }
    }

    private Episode createEpisodeFromMigrationData(Clinic clinic, PatientServiceIdentifier psi) {
        Episode episode = new Episode()
        episode.setClinic(clinic)
        episode.setEpisodeDate(this.prescriptiondate)
        String codeEpisodeType = this.stopdate == null ? "INICIO" : "FIM"
        episode.setEpisodeType(EpisodeType.findByCode(codeEpisodeType))
        String reason = this.stopdate == null ? this.startreason : this.stopreason

        StartStopReason startStopReason = StartStopReason.findByReasonIlike(this.stopdate == null ? "%" + this.startreason + "%" : "%" + this.stopreason + "%")
        if (startStopReason == null) {
            List<StartStopReason> startStopReasonList = StartStopReason.all
            for (StartStopReason startStopReason1 : startStopReasonList) {
                if (Utilities.compareStringIgnoringAccents(startStopReason1.getReason(), reason)) {
                    startStopReason = startStopReason1
                    break
                }
            }
            if (startStopReason == null) {
                for (StartStopReason startStopReason1 : startStopReasonList) {
                    if (Utilities.stripAccents(reason).toLowerCase().contains(Utilities.stripAccents(startStopReason1.getReason()).toLowerCase())) {
                        startStopReason = startStopReason1
                        break
                    }
                }
            }
            if (startStopReason == null) {
                startStopReason = StartStopReason.findByCode("OUTRO")
            }
        }
        episode.setStartStopReason(startStopReason)
        episode.setCreationDate(new Date())
        ClinicSector clinicSector = ClinicSector.findByCode(getClinicSectorCode())
        episode.setClinicSector(clinicSector)
        episode.setNotes(this.stopdate == null ? this.startnotes : this.stopnotes)
        episode.setEpisodeDate(this.stopdate == null ? this.startdate : this.stopdate)
        if (!Utilities.stringHasValue(episode.getNotes()) && this.startdate != null) {
            episode.setNotes(Utilities.stringHasValue(this.startreason) ? this.startreason : this.stopreason)
        }
        episode.setPatientServiceIdentifier(psi)

        return episode
    }

    private EpisodeMigrationRecord createEpisodeMigrationRecord(Episode episode) {
        EpisodeMigrationRecord episodeM = new EpisodeMigrationRecord()
        episodeM.setId(this.episodeid)
        episodeM.setMigratedRecord(episode)
        return episodeM
    }

    private Episode generateGenericEpisode(Clinic clinic, PatientServiceIdentifier patientServiceIdentifier) {
        Episode genericEpisode = new Episode()
        genericEpisode.setNotes("Episodio criado para prescricoes sem episodio no iDART")
        genericEpisode.setClinic(clinic)
        genericEpisode.setEpisodeDate(this.prescriptiondate)
        genericEpisode.setEpisodeType(EpisodeType.findByCode("INICIO"))
        StartStopReason startStopReason = StartStopReason.findByCode("OUTRO")
        genericEpisode.setStartStopReason(startStopReason)
        genericEpisode.setCreationDate(new Date())
        ClinicSector clinicSector = ClinicSector.findByCode(getClinicSectorCode())
        genericEpisode.setClinicSector(clinicSector)
        genericEpisode.setPatientServiceIdentifier(patientServiceIdentifier)
        return genericEpisode
    }

    Episode createEpisodeByStartStopReason(PatientServiceIdentifier patientServiceIdentifier, String reasonCode, String codeEpType) {
        Episode migrationEpisode = new Episode()
        //   migrationEpisode.id = UUID.randomUUID()
        migrationEpisode.startStopReason = StartStopReason.findByCode(reasonCode)
        migrationEpisode.episodeType = EpisodeType.findByCode(codeEpType)
        migrationEpisode.patientServiceIdentifier = patientServiceIdentifier
        migrationEpisode.clinic = patientServiceIdentifier.clinic
        migrationEpisode.clinicSector = ClinicSector.findByCode(getClinicSectorCode())
        migrationEpisode.episodeDate = this.prescriptiondate
        migrationEpisode.creationDate = new Date()
        migrationEpisode.notes = 'Episodio de Transito'
        //  migrationEpisode.save()
        return migrationEpisode
    }

    static String getNewPrescriptionId(PatientServiceIdentifier patientServiceIdentifier, Date theDate) {

        String newPrescriptionId = "";
        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");

        String latest = 'A';

        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            latest = latest.concat((random.nextInt(6) + 1).toString())
        }

        newPrescriptionId = df.format(theDate).concat(String.valueOf(latest))
                .concat("-").concat(patientServiceIdentifier.value);

        return newPrescriptionId;
    }

}