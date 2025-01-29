package mz.org.fgh.sifmoz.backend.episode

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import groovy.json.JsonSlurper
import liquibase.repackaged.org.apache.commons.lang3.time.DateUtils
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.convertDateUtils.ConvertDateUtils
import mz.org.fgh.sifmoz.backend.episodeType.EpisodeType
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.startStopReason.StartStopReason
import mz.org.fgh.sifmoz.backend.tansreference.IPatientTransReferenceService
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReference
import mz.org.fgh.sifmoz.backend.tansreference.PatientTransReferenceType
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer
import org.springframework.validation.BindingResult

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

import grails.gorm.transactions.Transactional

class EpisodeController extends RestfulController {

    IEpisodeService episodeService
    IPatientTransReferenceService referenceService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    EpisodeController() {
        super(Episode)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(episodeService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(episodeService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Episode episode = new Episode()

        def objectJSON = request.JSON
        episode = objectJSON as Episode

        episode.beforeInsert()
        episode.validate()

        if (objectJSON.id) {
            episode.id = UUID.fromString(objectJSON.id)
        }

        if (episode.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond episode.errors
            return
        }

        try {

            configEpisodeOrigin(episode)
            episodeService.save(episode)
            if (episode.startStopReason.code.equalsIgnoreCase(StartStopReason.TRANSFERIDO_PARA) ||
                    episode.startStopReason.code.equalsIgnoreCase(StartStopReason.OBITO)) {
                closePatientServiceIdentifierOfPatientWithTrasnferenceOrObitEpisode(episode)
            }
            if (episode.startStopReason.code.equalsIgnoreCase("TRANSFERIDO_PARA") ||
                    episode.startStopReason.code.equalsIgnoreCase("VOLTOU_REFERENCIA")) {
                patientTransReferenceCloseMobileEpisode(episode).save()
                createCloseEpisodeForOtherPatientIdentifiersWhenPatientReferred(episode)
            }
            if (episode.startStopReason.code.equalsIgnoreCase(StartStopReason.REFERIDO_SECTOR_CLINICO) ||
                    episode.startStopReason.code.equalsIgnoreCase("REFERIDO_PARA") ||
                episode.startStopReason.code.equalsIgnoreCase("REFERIDO_DC")) {
                patientTransReferenceCloseMobileEpisode(episode).save()
                createCloseEpisodeForOtherPatientIdentifiersWhenPatientReferred(episode)
                createStartEpisodeOnSectorAfterReferingToSector(episode)
            }

        } catch (ValidationException e) {
            respond episode.errors
            return
        }

        respond episode, [status: CREATED, view: "show"]
    }

    @Transactional
    def update() {
        def objectJSON = request.JSON
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")

        def auxEpisode = (parseTo(objectJSON.toString()) as Map) as Episode
        Episode episode = Episode.get(objectJSON.id)
        bindData(episode, auxEpisode, [exclude: ['id']])
        //updating db object
        episode.properties = auxEpisode.properties as BindingResult
        if (episode == null) {
            render status: NOT_FOUND
            return
        }
        if (episode.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond episode.errors
            return
        }

        try {
            configEpisodeOrigin(episode)
            episodeService.save(episode)
        } catch (ValidationException e) {
            respond episode.errors
            return
        }

        render JSONSerializer.setJsonObjectResponse(episode) as JSON
    }

    @Transactional
    def delete(String id) {
        List<String> startStopReasonList = ['REFERIDO_PARA', 'REFERIDO_SECTOR_CLINICO', 'REFERIDO_DC'].asList()

        if (id == null || episodeService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        Episode episode = Episode.findById(id)

        Episode episodeReferred = Episode.findByEpisodeDateBetweenAndIdNotEqualAndPatientServiceIdentifier(
                ConvertDateUtils.getDateAtStartOfDay(episode.episodeDate), ConvertDateUtils.getDateAtEndOfDay(episode.episodeDate), episode.id, episode.patientServiceIdentifier)

        if (episodeReferred !== null && startStopReasonList.contains(episodeReferred.startStopReason.getCode())) {
            episodeService.delete(episodeReferred.id)
        }
        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        respond episodeService.getAllByClinicId(clinicId, offset, max)
    }

    def getByIdentifierId(String identifierId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(episodeService.getAllByIndentifier(identifierId, offset, max)) as JSON
    }


    def getLastWithVisitByIndentifier(String identifierId, String cliniId) {
        render JSONSerializer.setJsonObjectResponse(episodeService.getLastWithVisitByIndentifier(PatientServiceIdentifier.findById(identifierId), Clinic.findById(cliniId))) as JSON
    }

    def getLastWithVisitByClinicSectors(String clinicSectorId) {
        render JSONSerializer.setObjectListJsonResponse(episodeService.getLastWithVisitByClinicAndClinicSector(ClinicSector.findById(clinicSectorId))) as JSON
    }

    private static def parseTo(String jsonString) {
        return new JsonSlurper().parseText(jsonString)
    }

    private static PatientTransReference patientTransReferenceCloseMobileEpisode(Episode episode) {

        def operationType = null
        def destination = episode.referralClinic
        if (episode.startStopReason.code.equalsIgnoreCase("TRANSFERIDO_PARA"))
            operationType = PatientTransReferenceType.findByCode("TRANSFERENCIA")
        else if (episode.startStopReason.code.equalsIgnoreCase("REFERIDO_DC")) {
            operationType = PatientTransReferenceType.findByCode("REFERENCIA_DC")
            destination = episode.clinicSector.uuid
        } else if (episode.startStopReason.code.equalsIgnoreCase("REFERIDO_PARA"))
            operationType = PatientTransReferenceType.findByCode("REFERENCIA_FP")
        else if (episode.startStopReason.code.equalsIgnoreCase("VOLTOU_REFERENCIA"))
            operationType = PatientTransReferenceType.findByCode("VOLTOU_DA_REFERENCIA")


        def transReference = new PatientTransReference()
        transReference.id = UUID.randomUUID().toString()
        transReference.syncStatus = 'P'
        transReference.operationDate = episode.episodeDate
        transReference.creationDate = new Date()
        transReference.operationType = operationType
        transReference.origin = episode.clinic
        transReference.destination = destination
        transReference.patient = episode.patientServiceIdentifier.patient
        transReference.identifier = episode.patientServiceIdentifier
        transReference.patientStatus = 'Activo'

        return transReference
    }

    public closePatientServiceIdentifierOfPatientWithTrasnferenceOrObitEpisode(Episode episode) {
        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(episode.patientServiceIdentifier.patient)

        patientServiceIdentifiers.each { item ->
            if (item.id == episode.patientServiceIdentifier.id) {
                item.endDate = episode.episodeDate
                item.state = 'Inactivo'
                item.save()
            } else {
                Episode closureEpisode = new Episode()
                closureEpisode.episodeDate = episode.episodeDate
                closureEpisode.episodeType = EpisodeType.findByCode('FIM')
                closureEpisode.patientServiceIdentifier = item
                closureEpisode.clinic = item.clinic
                closureEpisode.clinicSector = episode.clinicSector
                closureEpisode.creationDate = new Date()
                closureEpisode.notes = 'Fechado Devido ao' + episode.startStopReason.code
                closureEpisode.startStopReason = episode.startStopReason
                closureEpisode.origin = episode.clinic.uuid
                closureEpisode.residentInCountry = episode.residentInCountry
                closureEpisode.beforeInsert()
                episodeService.save(closureEpisode)
                item.endDate = episode.episodeDate
                item.state = 'Inactivo'
                item.origin = episode.origin
                item.save(flush: true)
            }

        }
    }

    def getAllByEpisodeIds() {
        def objectJSON = request.JSON
        List<String> ids = objectJSON
        render JSONSerializer.setObjectListJsonResponse(Episode.findAllByIdInList(ids)) as JSON
    }

    public createCloseEpisodeForOtherPatientIdentifiersWhenPatientReferred(Episode episode) {

        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(episode.patientServiceIdentifier.patient)
        patientServiceIdentifiers.each { item ->
            if (episode.patientServiceIdentifier.id != item.id) {
                Episode closureEpisode = new Episode()
                closureEpisode.episodeDate = episode.episodeDate
                closureEpisode.episodeType = EpisodeType.findByCode('FIM')
                closureEpisode.patientServiceIdentifier = item
                closureEpisode.clinic = item.clinic
                closureEpisode.clinicSector = episode.clinicSector
                closureEpisode.creationDate = new Date()
                closureEpisode.notes = 'Fechado Devido ao' + episode.startStopReason.code
                closureEpisode.startStopReason = episode.startStopReason
                closureEpisode.origin = episode.origin
                closureEpisode.residentInCountry = episode.residentInCountry
                closureEpisode.beforeInsert()
                episodeService.save(closureEpisode)
            }
        }
    }

    private createStartEpisodeOnSectorAfterReferingToSector(Episode episode) {
        def patientServiceIdentifiers = PatientServiceIdentifier.findAllByPatient(episode.patientServiceIdentifier.patient)
        patientServiceIdentifiers.each { item ->
            Episode openingEpisode = new Episode()
            openingEpisode.episodeDate = DateUtils.addMinutes(episode.episodeDate, 1)
            openingEpisode.episodeType = EpisodeType.findByCode('INICIO')
            openingEpisode.patientServiceIdentifier = item
            openingEpisode.clinic = episode.clinic
            openingEpisode.clinicSector = episode.clinicSector
            openingEpisode.creationDate = new Date()
            openingEpisode.notes = 'Aberto Devido ao' + episode.startStopReason.code
            openingEpisode.startStopReason = StartStopReason.findByCode('MANUNTENCAO')
            openingEpisode.origin = episode.origin
            openingEpisode.residentInCountry = episode.residentInCountry
            openingEpisode.beforeInsert()
            episodeService.save(openingEpisode)
        }
    }

    private static Episode configEpisodeOrigin(Episode episode) {
        SystemConfigs systemConfigs = SystemConfigs.findByKey("INSTALATION_TYPE")
        if (systemConfigs && systemConfigs.value.equalsIgnoreCase("LOCAL") && checkHasNotOrigin(episode)) {
            episode.origin = systemConfigs.description
        }

        return episode
    }

    private static boolean checkHasNotOrigin(Episode episode){
        return episode.origin == null || episode?.origin?.isEmpty()
    }
}
