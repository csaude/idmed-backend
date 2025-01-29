package mz.org.fgh.sifmoz.backend.migration.fromDataSource

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonManagedReference
import grails.gorm.transactions.Transactional
import grails.web.databinding.DataBinder
import grails.databinding.SimpleMapDataBindingSource
import groovy.json.JsonSlurper
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.clinicSectorType.ClinicSectorType
import mz.org.fgh.sifmoz.backend.dispenseMode.DispenseMode
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Localidade
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.PostoAdministrativo
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.duration.Duration
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.group.GroupPack
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.identifierType.IdentifierType
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import groovy.sql.Sql
import mz.org.fgh.sifmoz.backend.prescription.SpetialPrescriptionMotive
import mz.org.fgh.sifmoz.backend.prescriptionDetail.PrescriptionDetail
import mz.org.fgh.sifmoz.backend.prescriptionDrug.PrescribedDrug
import mz.org.fgh.sifmoz.backend.screening.AdherenceScreening
import mz.org.fgh.sifmoz.backend.screening.PregnancyScreening
import mz.org.fgh.sifmoz.backend.screening.RAMScreening
import mz.org.fgh.sifmoz.backend.screening.TBScreening
import mz.org.fgh.sifmoz.backend.screening.VitalSignsScreening
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import mz.org.fgh.sifmoz.backend.therapeuticLine.TherapeuticLine
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import javax.sql.DataSource

@Transactional
class DataSourceMigrationService implements DataBinder {

    @Autowired
    @Qualifier('dataSource_second')
    DataSource dataSource_second

    def loadAndSaveDoctorsMigrationService() {
        println("++++++++++INICIO CARREGAMENTO DOCTORS++++++++++")
        def sql = new Sql(dataSource_second)
        def doctorToMigrate = sql.rows("select * from doctor")
        Doctor.withTransaction {

            doctorToMigrate.each { doctorObject ->
                def doctor = Doctor.findById(doctorObject.id)
                if (!doctor) {
                    doctor = new Doctor()
                    doctor.id = doctorObject.id
                    doctor.firstnames = doctorObject.firstnames
                    doctor.lastname = doctorObject.lastname
                    doctor.gender = doctorObject.gender
                    doctor.telephone = doctorObject.telephone
                    doctor.email = doctorObject.email
                    doctor.clinic = Clinic.get(doctorObject.clinic_id)
                    doctor.active = doctorObject.active
                    doctor.save(flush: true, failOnError: true)
                }
            }
            println("++++++++++FIM CARREGAMENTO DOCTORS++++++++++")
        }

    }

    def loadAndSaveClinicSectorMigrationService() {
        println("++++++++++INICIO CARREGAMENTO CLINICSECTOR++++++++++")
        def sql = new Sql(dataSource_second)
        def clinicSectorToMigrate = sql.rows("select * from clinic_sector")
        ClinicSector.withTransaction {

            clinicSectorToMigrate.each { clinicSectorObject ->
                def clinicSector = ClinicSector.findById(clinicSectorObject.id)
                if (!clinicSector) {
                    clinicSector = new ClinicSector()
                    clinicSector.id = clinicSectorObject.id
                    clinicSector.code = clinicSectorObject.code
                    clinicSector.description = clinicSectorObject.description
                    clinicSector.uuid = clinicSectorObject.uuid
                    clinicSector.active = clinicSectorObject.active
                    clinicSector.syncStatus = clinicSectorObject.sync_status
                    clinicSector.clinicSectorType = ClinicSectorType.get(clinicSectorObject.clinic_sector_type_id)
                    clinicSector.clinic = Clinic.get(clinicSectorObject.clinic_id)

                    clinicSector.save(flush: true, failOnError: true)
                }
            }
            println("++++++++++FIM CARREGAMENTO CLINICSECTOR++++++++++")
        }
    }

    def loadAndSavePatientVisitDetailsMigrationService() {
        println("++++++++++INICIO CARREGAMENTO CASCATA PATIENT VISIT DETAILS++++++++++")
        def sql = new Sql(dataSource_second)
        def PatientVisitDetailsToMigrate = sql.rows("select * from patient_visit_details where creation_date is not null")
        PatientVisitDetails.withTransaction {
        def totalLinhas =  PatientVisitDetailsToMigrate.size()
            PatientVisitDetailsToMigrate.each { pvdObject ->
                printf("")
                println("Count Down "+ totalLinhas--)
                def dataTosaveAlreadyExist = PatientVisitDetails.findById(pvdObject.id)

                if (!dataTosaveAlreadyExist) {
                    PatientVisitDetails patientVisitDetails = new PatientVisitDetails()
                    patientVisitDetails.id = pvdObject.id
                    patientVisitDetails.clinic = Clinic.findById(pvdObject.clinic_id)
                    patientVisitDetails.prescription = checkAndSavePrescrition(pvdObject.prescription_id)
                    patientVisitDetails.patientVisit = checkAndSavePatientVisit(pvdObject.patient_visit_id)
                    patientVisitDetails.episode = checkAndSaveEpisode(pvdObject.episode_id)
                    patientVisitDetails.pack = checkAndSavePack(pvdObject.pack_id)

                    patientVisitDetails.save(flush: true, failOnError: true)
                }
            }
            println("++++++++++FIM CARREGAMENTO CASCATA PATIENT VISIT DETAILS++++++++++")
        }

    }

    def loadAndSaveEpisodeMigrationService() {
        println("++++++++++INICIO CARREGAMENTO CASCATA EPISODES++++++++++")
        def sql = new Sql(dataSource_second)
        def episodeToMigrate = sql.rows("select * from episode where creation_date is not null")
        def totalLinhas =  episodeToMigrate.size()
        episodeToMigrate.each { episodeObject ->
            printf("")
            println("Count Down "+ totalLinhas--)
            checkAndSaveEpisode(episodeObject.id)
        }
        println("++++++++++FIM CARREGAMENTO CASCATA EPISODES++++++++++")
    }

    def loadAndSavePatientVisitMigrationService() {
        println("++++++++++INICIO CARREGAMENTO CASCATA PATIENT VISIT++++++++++")
        def sql = new Sql(dataSource_second)
        def patientVisitToMigrate = sql.rows("select * from patient_visit where creation_date is not null")
        def totalLinhas =  patientVisitToMigrate.size()
        patientVisitToMigrate.each { patientVisitObject ->
            printf("")
            println("Count Down "+ totalLinhas--)
            checkAndSavePatientVisit(patientVisitObject.id)
        }
        println("++++++++++FIM CARREGAMENTO CASCATA PATIENT VISIT++++++++++")
    }

    private Prescription checkAndSavePrescrition(String prescriptionId) {
        def sql = Sql.newInstance(dataSource_second)
        def prescriptionToMigrate = sql.rows("select * from prescription where id = ?", [prescriptionId])
        def prescriptionDetailToMigrate = sql.rows("select * from prescription_detail where prescription_id = ?", [prescriptionId])
        def prescribedDrugToMigrate = sql.rows("select * from prescribed_drug where prescription_id = ?", [prescriptionId])

        Prescription.withTransaction {
            Prescription prescription = Prescription.findById(prescriptionId)
            def prescriptionObject = prescriptionToMigrate.first()
            def doctor = Doctor.findById(prescriptionObject.doctor_id)
            if (!prescription) {
                prescription = new Prescription()
                prescription.prescriptionDate = prescriptionObject.prescription_date
                prescription.expiryDate = prescriptionObject.expiry_date
                prescription.current = prescriptionObject.current
                prescription.notes = prescriptionObject.notes
                prescription.prescriptionSeq = prescriptionObject.prescription_seq
                prescription.patientType = prescriptionObject.patient_type
                prescription.patientStatus = prescriptionObject.patient_status
                prescription.modified = prescriptionObject.modified
                prescription.clinic = Clinic.findById(prescriptionObject.clinic_id)
                prescription.doctor = Doctor.findById(prescriptionObject.doctor_id)
                prescription.duration = Duration.findById(prescriptionObject.duration_id)
                prescription.photo = prescriptionObject.photo
                prescription.photoName = prescriptionObject.photo_name
                prescription.photoContentType = prescriptionObject.photo_content_type
                prescription.creationDate = prescriptionObject.creation_date
                prescription.id = prescriptionId

                prescriptionDetailToMigrate.each { prescriptionDetailObject ->
                    def prescriptionDetail = new PrescriptionDetail()
                    prescriptionDetail.id = prescriptionDetailObject.id
                    prescriptionDetail.reasonForUpdate = prescriptionDetailObject.reason_for_update
                    prescriptionDetail.therapeuticLine = TherapeuticLine.get(prescriptionDetailObject.therapeutic_line_id)
                    prescriptionDetail.therapeuticRegimen = TherapeuticRegimen.get(prescriptionDetailObject.therapeutic_regimen_id)
                    prescriptionDetail.dispenseType = DispenseType.get(prescriptionDetailObject.dispense_type_id)
                    prescriptionDetail.spetialPrescriptionMotive = SpetialPrescriptionMotive.get(prescriptionDetailObject.spetial_prescription_motive_id)
                    prescriptionDetail.creationDate = prescriptionDetailObject.creation_date

                    prescription.addToPrescriptionDetails(prescriptionDetail)
                }

                prescribedDrugToMigrate.each { prescribedDrugObject ->

                    def prescribedDrug = new PrescribedDrug()
                    prescribedDrug.id = prescribedDrugObject.id
                    prescribedDrug.amtPerTime = prescribedDrugObject.amt_per_time
                    prescribedDrug.timesPerDay = prescribedDrugObject.times_per_day
                    prescribedDrug.prescribedQty = prescribedDrugObject.prescribed_qty
                    prescribedDrug.form = prescribedDrugObject.form
                    prescribedDrug.modified = prescribedDrugObject.modified
                    prescribedDrug.drug = Drug.get(prescribedDrugObject.drug_id)

                    prescription.addToPrescribedDrugs(prescribedDrug)
                }

                prescription.save(flush: true, failOnError: true)
            }
            return prescription
        }
    }

    private Episode checkAndSaveEpisode(String episodeId) {
        def sql = new Sql(dataSource_second)
        def episodeToMigrate = sql.rows("select * from episode where id = ?", [episodeId])
        Episode.withTransaction {
            def episodeObject = episodeToMigrate.first()
            Episode episode = Episode.findById(episodeId)
            if (!episode) {
                episode = new Episode()
                episode.id = episodeObject.id
                episode.episodeDate = episodeObject.episode_date
                episode.creationDate = episodeObject.creation_date
                episode.startStopReason = StartStopReason.get(episodeObject.start_stop_reason_id)
                episode.notes = episodeObject.notes
                episode.episodeType = EpisodeType.get(episodeObject.episode_type_id)
                episode.clinicSector = ClinicSector.get(episodeObject.clinic_sector_id)
                episode.clinic = Clinic.get(episodeObject.clinic_id)
                episode.referralClinic = Clinic.get(episodeObject.referral_clinic_id)
                episode.patientServiceIdentifier = checkAndSavePatientServiceIdentifier(episodeObject.patient_service_identifier_id)
                episode.residentInCountry = episodeObject.resident_in_country
                episode.save(flush: true, failOnError: true)
            }
            return episode
        }
    }

    private PatientServiceIdentifier checkAndSavePatientServiceIdentifier(String patientServiceIdentifierId) {
        def sql = new Sql(dataSource_second)
        def patientServiceIdentifierToMigrate = sql.rows("select * from patient_service_identifier where id = ?", [patientServiceIdentifierId])
        def patientServiceIdentifierObject = patientServiceIdentifierToMigrate.first()

        PatientServiceIdentifier.withTransaction {
            def patientServiceIdentifier = PatientServiceIdentifier.findByValueAndService(patientServiceIdentifierObject.value, ClinicalService.get(patientServiceIdentifierObject.service_id))
            if (!patientServiceIdentifier) {
                patientServiceIdentifier = new PatientServiceIdentifier()
                patientServiceIdentifier.id = patientServiceIdentifierObject.id
                patientServiceIdentifier.startDate = patientServiceIdentifierObject.start_date
                patientServiceIdentifier.endDate = patientServiceIdentifierObject.end_date
                patientServiceIdentifier.reopenDate = patientServiceIdentifierObject.reopen_date
                patientServiceIdentifier.value = patientServiceIdentifierObject.value
                patientServiceIdentifier.state = patientServiceIdentifierObject.state
                patientServiceIdentifier.prefered = patientServiceIdentifierObject.prefered
                patientServiceIdentifier.identifierType = IdentifierType.get(patientServiceIdentifierObject.identifier_type_id)
                patientServiceIdentifier.service = ClinicalService.get(patientServiceIdentifierObject.service_id)
                patientServiceIdentifier.clinic = Clinic.get(patientServiceIdentifierObject.clinic_id)
                patientServiceIdentifier.patient = checkAndSavePatient(patientServiceIdentifierObject.patient_id)

                patientServiceIdentifier.save(flush: true, failOnError: true)
            }
            return patientServiceIdentifier
        }
    }

    private PatientVisit checkAndSavePatientVisit(String patientVisitId) {
        def sql = new Sql(dataSource_second)
        def patientVisitToMigrate = sql.rows("select * from patient_visit where id = ?", [patientVisitId])
        def adherenceScreeningToMigrate = sql.rows("select * from adherence_screening where visit_id = ?", [patientVisitId])
        def vitalSignsScreeningToMigrate = sql.rows("select * from vital_signs_screening where visit_id = ?", [patientVisitId])
        def pregnancyScreeningToMigrate = sql.rows("select * from pregnancy_screening where visit_id = ?", [patientVisitId])
        def tbScreeningToMigrate = sql.rows("select * from tbscreening where visit_id = ?", [patientVisitId])
        def ramScreeningsToMigrate = sql.rows("select * from ramscreening where visit_id = ?", [patientVisitId])

        PatientVisit.withTransaction {
            PatientVisit patientVisit = PatientVisit.findById(patientVisitId)
            def patientVisitObject = patientVisitToMigrate.first()
            if (!patientVisit) {
                patientVisit = new PatientVisit()
                patientVisit.id = patientVisitObject.id
                patientVisit.visitDate = patientVisitObject.visit_date
                patientVisit.clinic = Clinic.get(patientVisitObject.clinic_id)
                patientVisit.creationDate = patientVisitObject.creation_date
                patientVisit.patient = checkAndSavePatient(patientVisitObject.patient_id)

                adherenceScreeningToMigrate.each { adherenceScreeningObject ->
                    def adherenceScreening = new AdherenceScreening()
                    adherenceScreening.id = adherenceScreeningObject.id
                    adherenceScreening.hasPatientCameCorrectDate = adherenceScreeningObject.has_patient_came_correct_date
                    adherenceScreening.daysWithoutMedicine = adherenceScreeningObject.days_without_medicine
                    adherenceScreening.patientForgotMedicine = adherenceScreeningObject.patient_forgot_medicine
                    adherenceScreening.lateDays = adherenceScreeningObject.late_days
                    adherenceScreening.lateMotives = adherenceScreeningObject.late_motives

                    patientVisit.addToAdherenceScreenings(adherenceScreening)
                }

                vitalSignsScreeningToMigrate.each { vitalSignsScreeningObject ->
                    def vitalSignsScreening = new VitalSignsScreening()
                    vitalSignsScreening.id = vitalSignsScreeningObject.id
                    vitalSignsScreening.distort = vitalSignsScreeningObject.distort
                    vitalSignsScreening.imc = vitalSignsScreeningObject.imc
                    vitalSignsScreening.weight = vitalSignsScreeningObject.weight
                    vitalSignsScreening.systole = vitalSignsScreeningObject.systole
                    vitalSignsScreening.height = vitalSignsScreeningObject.height

                    patientVisit.addToVitalSignsScreenings(vitalSignsScreening)
                }

                pregnancyScreeningToMigrate.each { pregnancyScreeningObject ->

                    def pregnancyScreening = new PregnancyScreening()
                    pregnancyScreening.id = pregnancyScreeningObject.id
                    pregnancyScreening.pregnant = pregnancyScreeningObject.pregnant
                    pregnancyScreening.menstruationLastTwoMonths = pregnancyScreeningObject.menstruation_last_two_months
                    pregnancyScreening.lastMenstruation = pregnancyScreeningObject.last_menstruation

                    patientVisit.addToPregnancyScreenings(pregnancyScreening)
                }

                tbScreeningToMigrate.each { tbScreeningObject ->

                    def tbScreening = new TBScreening()
                    tbScreening.id = tbScreeningObject.id
                    tbScreening.parentTBTreatment = tbScreeningObject.parent_tB_treatment
                    tbScreening.cough = tbScreeningObject.cough
                    tbScreening.fever = tbScreeningObject.fever
                    tbScreening.losingWeight = tbScreeningObject.losing_weight
                    tbScreening.treatmentTB = tbScreeningObject.treatment_tB
                    tbScreening.treatmentTPI = tbScreeningObject.treatment_tPI
                    tbScreening.startTreatmentDate = tbScreeningObject.start_treatment_date
                    tbScreening.fatigueOrTirednessLastTwoWeeks = tbScreeningObject.fatigue_or_tiredness_last_two_weeks
                    tbScreening.sweating = tbScreeningObject.sweating

                    patientVisit.addToTbScreenings(tbScreening)
                }

                ramScreeningsToMigrate.each { ramScreeningsObject ->
                    def ramScreenings = new RAMScreening()
                    ramScreenings.id = ramScreeningsObject.id
                    ramScreenings.adverseReaction = ramScreeningsObject.adverse_reaction
                    ramScreenings.adverseReactionMedicine = ramScreeningsObject.adverse_reaction_medicine

                    patientVisit.addToRamScreenings(ramScreenings)
                }

                patientVisit.save(flush: true, failOnError: true)
            }
            return patientVisit
        }
    }

    private Patient checkAndSavePatient(String patientId) {
        def sql = new Sql(dataSource_second)
        def patientsToMigrate = sql.rows("select * from patient p where id = ? ", [patientId])
        def patientsIdentifierToMigrate = sql.rows("select * from patient_service_identifier p where id = ? ", [patientId])

        def patientObject = patientsToMigrate.first()
        def patient = Patient.findById(patientObject.id)

        if (!patient) {
            patient = new Patient()
            patient.id = patientObject.id
            patient.firstNames = patientObject.first_names
            patient.middleNames = patientObject.middle_names
            patient.lastNames = patientObject.last_names
            patient.gender = patientObject.gender
            patient.dateOfBirth = patientObject.date_of_birth
            patient.cellphone = patientObject.cellphone
            patient.alternativeCellphone = patientObject.alternative_cellphone
            patient.address = patientObject.address
            patient.addressReference = patientObject.address_reference
            patient.accountstatus = patientObject.accountstatus
            patient.hisUuid = patientObject.his_uuid
            patient.hisLocation = patientObject.his_location
            patient.hisLocationName = patientObject.his_location_name
            patient.his = HealthInformationSystem.get(patientObject.his_id)
            patient.province = Province.get(patientObject.province_id)
            patient.bairro = Localidade.get(patientObject.bairro_id)
            patient.district = District.get(patientObject.district_id)
            patient.postoAdministrativo = PostoAdministrativo.get(patientObject.posto_administrativo_id)
            patient.clinic = Clinic.get(patientObject.clinic_id)
            patient.creationDate = patientObject.creation_date

            patientsIdentifierToMigrate.each { patientsIdentifierObject ->

                def patientsIdentifier = new PatientServiceIdentifier()
                patientsIdentifier.id = patientsIdentifierObject.id
                patientsIdentifier.startDate = patientsIdentifierObject.start_date
                patientsIdentifier.endDate = patientsIdentifierObject.end_date
                patientsIdentifier.reopenDate = patientsIdentifierObject.reopen_date
                patientsIdentifier.value = patientsIdentifierObject.value
                patientsIdentifier.state = patientsIdentifierObject.state
                patientsIdentifier.prefered = patientsIdentifierObject.prefered
                patientsIdentifier.identifierType = IdentifierType.get(patientsIdentifierObject.identifier_type_id)
                patientsIdentifier.service = ClinicalService.get(patientsIdentifierObject.service_id)
                patientsIdentifier.clinic = Clinic.get(patientsIdentifierObject.clinic_id)

                patient.addToIdentifiers(patientsIdentifier)
            }

            patient.save(flush: true, failOnError: true)
        }
        return patient
    }

    private Pack checkAndSavePack(String packId) {
        def sql = new Sql(dataSource_second)
        def packToMigrate = sql.rows("select * from pack where id = ?", [packId])
        def packagedDrugToMigrate = sql.rows("select * from packaged_drug where pack_id = ?", [packId])

        Pack.withTransaction {
            def packObject = packToMigrate.first()
            Pack pack = Pack.findById(packId)

            if (!pack) {
                pack = new Pack()
                pack.id = packObject.id
                pack.dateLeft = packObject.date_left
                pack.dateReceived = packObject.date_received
                pack.modified = packObject.modified
                pack.packDate = packObject.pack_date
                pack.pickupDate = packObject.pickup_date
                pack.nextPickUpDate = packObject.next_pick_up_date
                pack.weeksSupply = packObject.weeks_supply
                pack.dateReturned = packObject.date_returned
                pack.stockReturned = packObject.stock_returned
                pack.packageReturned = packObject.package_returned
                pack.reasonForPackageReturn = packObject.reason_for_package_return
                pack.clinic = Clinic.get(packObject.clinic_id)
                pack.dispenseMode = DispenseMode.get(packObject.dispense_mode_id)
                pack.groupPack = GroupPack.get(packObject.group_pack_id)
                pack.syncStatus = packObject.sync_status
                pack.providerUuid = packObject.provider_uuid
                pack.creationDate = packObject.creation_date

                packagedDrugToMigrate.each { packagedDrugObject ->

                    def packagedDrug = new PackagedDrug()
                    packagedDrug.id = packagedDrugObject.id
                    packagedDrug.drug = Drug.get(packagedDrugObject.drug_id)
                    packagedDrug.amtPerTime = packagedDrugObject.amt_per_time
                    packagedDrug.timesPerDay = packagedDrugObject.times_per_day
                    packagedDrug.form = packagedDrugObject.form
                    packagedDrug.quantitySupplied = packagedDrugObject.quantity_supplied
                    packagedDrug.nextPickUpDate = packagedDrugObject.next_pick_up_date
                    packagedDrug.toContinue = packagedDrugObject.to_continue
                    packagedDrug.creationDate = packagedDrugObject.creation_date
                    pack.addToPackagedDrugs(packagedDrug)
                }
                pack.save(flush: true, failOnError: true)
            }
            return pack
        }
    }
}
