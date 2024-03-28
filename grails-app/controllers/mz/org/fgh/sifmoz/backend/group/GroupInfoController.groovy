package mz.org.fgh.sifmoz.backend.group

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.episode.IEpisodeService
import mz.org.fgh.sifmoz.backend.groupMember.GroupMember
import mz.org.fgh.sifmoz.backend.groupMember.IGroupMemberService
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.patientVisitDetails.PatientVisitDetails
import mz.org.fgh.sifmoz.backend.prescription.IPrescriptionService
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT

import grails.gorm.transactions.Transactional

class GroupInfoController extends RestfulController{

    IGroupService groupService
    IEpisodeService episodeService
    IGroupMemberService groupMemberService
    IPrescriptionService prescriptionService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    GroupInfoController() {
        super(GroupInfo)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(groupService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(groupService.get(id)) as JSON
    }

    @Transactional
    def save() {
        GroupInfo group = new GroupInfo()
        def objectJSON = request.JSON
        group = objectJSON as GroupInfo

        group.validate()

        if(objectJSON.id){
            group.id = UUID.fromString(objectJSON.id)
            group.members.eachWithIndex { item, index ->
                item.id = UUID.fromString(objectJSON.members[index].id)
            }
        }

        if (group.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond group.errors
            return
        }
        group.beforeInsert()
        HashSet<GroupMember> memberList = group.members
        try {
            group.members = null
            groupService.save(group)
            for (GroupMember member : memberList) {
                member.group = group
                groupMemberService.save(member)
            }
        } catch (ValidationException e) {
            transactionStatus.setRollbackOnly()
            respond group.errors
            return
        }

        def memberListJson = JSONSerializer.setObjectListJsonResponse(memberList as List)
        def result = JSONSerializer.setJsonObjectResponse(group)
        if (memberListJson)
            result.put('members', memberListJson)
        else
            result.remove('members')
        render result as JSON
        //   respond group, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {

        GroupInfo group
        def objectJSON = request.JSON

        if(objectJSON.id){
            group = GroupInfo.get(objectJSON.id)
            if (group == null) {
                render status: NOT_FOUND
                return
            }
            group.properties = objectJSON
            group.members.eachWithIndex { item, index ->
                objectJSON.members.eachWithIndex { item2 ,index2 ->
                    if (item.patientId == objectJSON.members[index2].patient_id) {
                        item.id = UUID.fromString(objectJSON.members[index2].id)
                    }
                }

            }

        }

        if (group.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond group.errors
            return
        }

        try {
          List<GroupMember> membersToRemove = new ArrayList<>()
            group.members.each { item ->
         GroupMember existingGroupMember = GroupMember.findByPatientAndGroup(item.getPatient(),group)
                if (existingGroupMember != null && existingGroupMember.endDate != null) {
                    existingGroupMember.properties = item
                   groupMemberService.save(existingGroupMember)
                    membersToRemove.add(item)
                }
            }
           group.members.removeAll(membersToRemove)
            groupService.save(group)
        } catch (ValidationException e) {
            respond group.errors
            return
        }
        def memberListJson = JSONSerializer.setObjectListJsonResponse(group.members as List)
        def result = JSONSerializer.setJsonObjectResponse(group)
        if (memberListJson)
            result.put('members', memberListJson)
        else
            result.remove('members')
        render result as JSON
      //  respond group, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || groupService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByClinicId(String clinicId, int offset, int max) {
        render JSONSerializer.setObjectListJsonResponse(groupService.getAllByClinicId(clinicId, offset, max)) as JSON
        //respond patientService.getAllByClinicId(clinicId, offset, max)
    }

    def validadePatient(String patientId, String serviceCode,String dispenseTypeCode) {
        Patient patient = Patient.findById(patientId)
        Set <GroupInfo> patientActiveGroups = groupService.getAllActiveOfPatientOnService(patient, serviceCode)
       // patient.setGroupMembers(groupService.getAllActiveOfPatientOnService(patient, serviceCode))
        String msg = "Accepted"

        Episode lastPatientEpisode = episodeService.getLastEpisodeByIdentifier(patient, serviceCode)

        PatientVisitDetails patientVisitDetailsByEpisode = PatientVisitDetails.findByEpisode(lastPatientEpisode)
        Prescription lastPrescription = prescriptionService.getLastPrescriptionByPatientId(patientId)

        if (lastPatientEpisode == null) {
            msg = "O paciente que pretende adicionar não pertence ao serviço ${serviceCode}."
           // render msg
        } else if (!lastPatientEpisode.startStopReason.isStartReason) {
            msg = "O Último episódio do paciente não é de inicio."
           // render msg
        } else if (patientVisitDetailsByEpisode == null) {
            msg = "O Paciente que pretende adicionar não tem nenhuma Dispensa no serviço ${serviceCode}."
           // render msg
        } else if (patient.isActiveOnGroup(serviceCode, patientActiveGroups)) {
            msg = "O paciente selecionado ja se encontra associado a um grupo activo do serviço [" + serviceCode + "]"
           // render msg
        } else if (lastPrescription.prescriptionDetails.getAt(0).dispenseType.code != dispenseTypeCode) {
            msg = "A ultima prescricao do paciente que pretende adcionar ao grupo nao faz parte do tipo de dispensa  [" + dispenseTypeCode + "]"
        }
        render msg
    }
}
