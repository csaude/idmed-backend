package mz.org.fgh.sifmoz.backend.episode

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisitDetails.IPatientVisitDetailsService
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.reports.referralManagement.IReferredPatientsReportService
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import org.springframework.beans.factory.annotation.Autowired

@Transactional
@Service(Episode)
abstract class EpisodeService implements IEpisodeService{

    IReferredPatientsReportService referredPatientsReportService
    IPatientVisitDetailsService iPatientVisitDetailsService

    @Override
    List<Episode> getAllByClinicId(String clinicId, int offset, int max) {
        Clinic clinic = Clinic.findWhere(id: clinicId)
        List<Episode> episodes = Episode.findAllWhere(clinic: clinic,[offset: offset, max: max])
        return episodes
    }
    @Override
    List<Episode> getAllByIndentifier(String identifierId, int offset, int max) {
        PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findWhere(id:  identifierId)
        def episodes = Episode.findAllWhere(patientServiceIdentifier: patientServiceIdentifier, [sort: ['episodeDate': 'desc'], max: 4])
        return episodes
    }

    @Override
    List<Episode> getEpisodeOfReferralOrBackReferral(Clinic clinic, ClinicalService clinicalService, String startStopReasonCode, Date startDate, Date endDate) {

        StartStopReason startStop = StartStopReason.findWhere(code: startStopReasonCode)
       // render Episode.findAllByStartStopReasonAndPatientServiceIdentifierInListAndEpisodeDateBetween(startStop,patients,startDate,endDate)
        return Episode.executeQuery("select ep from Episode ep " +
                "inner join ep.startStopReason stp " +
                "inner join ep.patientServiceIdentifier psi " +
                "inner join psi.patient p "+
                "inner join ep.clinic c " +
                "where c.id= ?0 and stp.id = ?1 and psi.service.id = ?2 and ep.episodeDate >= ?3 and ep.episodeDate <= ?4 and ep.episodeDate = (select max(ep2.episodeDate) " +
                "from Episode ep2 " +
                "inner join ep2.patientServiceIdentifier psi2 " +
                "inner join psi2.patient p2 " +
                "inner join ep2.clinic c2 " +
                "where p.id = p2.id and psi.id = psi2.id and c2.id = ?0 and ep2.episodeDate >= ?3 and ep2.episodeDate <= ?4) ", [clinic.id, startStop.id, clinicalService.id, startDate, endDate])
    }

    @Override
    Episode getEpisodeOfReferralByPatientServiceIdentfierAndBelowEpisodeDate(PatientServiceIdentifier patientServiceIdentifier, Date episodeDate) {
       // StartStopReason startStop = StartStopReason.findByCode("REFERIDO_PARA")

        def episodes = Episode.executeQuery("select ep from Episode ep " +
                "inner join ep.startStopReason stp " +
                "inner join ep.patientServiceIdentifier psi " +
                "inner join psi.patient p "+
                "inner join ep.clinic c " +
                "where psi = :patientServiceIdentifier and stp.code = 'REFERIDO_PARA' and ep.episodeDate <= :episodeDate order by ep.episodeDate desc", [patientServiceIdentifier:patientServiceIdentifier, episodeDate:episodeDate])

        return episodes.get(0)
    }

    @Override
    Episode getLastWithVisitByIndentifier(PatientServiceIdentifier patientServiceIdentifier, Clinic clinic) {
        def episodes = Episode.executeQuery("select ep from Episode ep " +
                "inner join ep.startStopReason stp " +
                "inner join ep.patientServiceIdentifier psi " +
                "inner join ep.clinic c " +
                "where psi = :patientServiceIdentifier " +
                "and exists (select pvd from PatientVisitDetails pvd where pvd.episode = ep ) " +
                //"and ep.clinic = :clinic" +
                "order by ep.episodeDate desc", [patientServiceIdentifier: patientServiceIdentifier])
        Episode episode = episodes.get(0)
        PatientVisitDetails patientVisitDetails = iPatientVisitDetailsService.getLastVisitByEpisodeId(episode.id)
        patientVisitDetails.setPrescription(Prescription.findWhere(id:  patientVisitDetails.prescription.id))
       // episode.setPatientVisitDetails(new HashSet<PatientVisitDetails>())
       // episode.getPatientVisitDetails().add(patientVisitDetails)
        return episode
    }

    @Override
    Episode getLastInitialEpisodeByIdentifier(String identifierId) {
       EpisodeType episodeType = EpisodeType.findWhere(code:  "INICIO")
        PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findWhere(id:  identifierId)
        def episode = Episode.findWhere(patientServiceIdentifier: patientServiceIdentifier, episodeType: episodeType, [sort: ['episodeDate': 'desc']])

        return episode
    }

    @Override
    Episode getLastEpisodeByIdentifier(Patient patient, String serviceCode) {
        ClinicalService clinicalService = ClinicalService.findWhere(code:  serviceCode)
        PatientServiceIdentifier patientServiceIdentifier = PatientServiceIdentifier.findWhere(patient: patient, service: clinicalService)
        Episode episode = Episode.findWhere(patientServiceIdentifier: patientServiceIdentifier, [sort: ['episodeDate': 'desc']])
        return episode
    }

    @Override
    List<Episode> getLastWithVisitByClinicAndClinicSector(ClinicSector clinicSector) {

        def episodes = Episode.executeQuery("select  ep from Episode ep " +
                "inner join ep.startStopReason stp " +
                "inner join ep.patientServiceIdentifier psi " +
                "inner join psi.patient p" +
                "inner join ep.clinic c " +
                "where ep.clinicSector = :clinicSector " +
                "and ep.episodeDate = ( " +
                "  SELECT MAX(e.episodeDate)" +
                "  FROM Episode e" +
                "  WHERE e.patientServiceIdentifier = ep.patientServiceIdentifier" +
                ")" +
                //"and ep.clinic = :clinic" +
                "order by ep.episodeDate desc", [clinicSector: clinicSector])
        episodes.each { it ->
   //         PatientVisitDetails patientVisitDetails = iPatientVisitDetailsService.getLastVisitByEpisodeId(it.id)
   //         patientVisitDetails.setPrescription(Prescription.findById(patientVisitDetails.prescription.id))
      //      it.setPatientVisitDetails(new HashSet<PatientVisitDetails>())
      //      it.getPatientVisitDetails().add(patientVisitDetails)
        }
        return episodes
    }

    @Override
    closePatientServiceIdentifierOfPatientWhenOpenMrsObitOrTransferred(Patient patient,String statusCode,Date statusDate) {
        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(patient)
        StartStopReason startStopReason = StartStopReason.findByCode(statusCode)
        patientServiceIdentifiers.each { item ->
            Episode lastEpisode = item.episodes.stream().reduce((prev, next) -> next).orElse(null)
            if (lastEpisode.startStopReason == startStopReason) {
                Episode closureEpisode = new Episode()
                closureEpisode.episodeDate = statusDate
                closureEpisode.episodeType = EpisodeType.findByCode('FIM')
                closureEpisode.patientServiceIdentifier = item
                closureEpisode.clinic = item.clinic
                closureEpisode.clinicSector = lastEpisode.getClinicSector()
                closureEpisode.creationDate = new Date()
                closureEpisode.notes = 'Fechado Devido ao ' + startStopReason
                closureEpisode.startStopReason = startStopReason
                closureEpisode.origin = lastEpisode.getClinic().getUuid()
                closureEpisode.beforeInsert()
                this.save(closureEpisode)
                item.endDate = new Date()
                item.state = 'Inactivo'
                item.origin = lastEpisode.getClinic().getUuid()
                item.save(flush: true)
            }
        }
        }
    @Override
    closeEpisodeWhenOpenmrsStatusCodeAbandonAndSuspended(Patient patient, String statusCode,Date statusDate){
        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(patient)
     StartStopReason startStopReason = StartStopReason.findByCode(statusCode)
        patientServiceIdentifiers.each { item ->
            Episode lastEpisode =  item.episodes.stream().reduce((prev, next) -> next).orElse(null)
            if (lastEpisode.startStopReason == startStopReason) {
                Episode closureEpisode = new Episode()
                closureEpisode.episodeDate = statusDate
                closureEpisode.episodeType = EpisodeType.findByCode('FIM')
                closureEpisode.patientServiceIdentifier = item
                closureEpisode.clinic = item.clinic
                closureEpisode.clinicSector = lastEpisode.getClinicSector()
                closureEpisode.creationDate = new Date()
                closureEpisode.notes = 'Fechado Devido ao' + startStopReason
                closureEpisode.startStopReason = startStopReason
                closureEpisode.origin = lastEpisode.getClinic().getUuid()
                closureEpisode.beforeInsert()
                this.save(closureEpisode)
            }
        }

    }

    @Override
    reopenEpisodeAndServiceWhenPatientActiveInSesp(Patient patient) {
        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(patient)
        List<String> startStopReasonList = ['ABANDONO', 'OBITO', 'SUSPENSO','TRANSFERIDO_PARA'].asList()
        List<String> stopPatientService = [ 'OBITO','TRANSFERIDO_PARA'].asList()

        patientServiceIdentifiers.each { item ->
            Episode lastEpisode =  item.episodes.stream().reduce((prev, next) -> next).orElse(null)
            if (startStopReasonList.contains(lastEpisode.startStopReason.getCode())) {
                Episode openingEpisode = new Episode()
                openingEpisode.episodeDate = new Date()
                openingEpisode.episodeType = EpisodeType.findByCode('INICIO')
                openingEpisode.patientServiceIdentifier = item
                openingEpisode.clinic = item.clinic
                openingEpisode.clinicSector = lastEpisode.getClinicSector()
                openingEpisode.creationDate = new Date()
                openingEpisode.notes = 'Aberto Devido a Reabertura no SESP'
                openingEpisode.startStopReason = StartStopReason.findByCode(StartStopReason.MANUNTENCAO)
                openingEpisode.origin = lastEpisode.getClinic().getUuid()
                openingEpisode.beforeInsert()
                this.save(openingEpisode)
            }
            if (stopPatientService.contains(lastEpisode.startStopReason.getCode())) {
                item.reopenDate = new Date()
                item.state = 'Activo'
                item.origin = lastEpisode.getClinic().getUuid()
                item.save(flush: true)
            }
        }
    }

}
