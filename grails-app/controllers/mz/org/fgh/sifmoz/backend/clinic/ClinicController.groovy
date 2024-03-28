package mz.org.fgh.sifmoz.backend.clinic

import grails.converters.JSON
import grails.rest.RestfulController
import grails.validation.ValidationException

import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.group.GroupInfo
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.JSONSerializer

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

import grails.gorm.transactions.Transactional

class ClinicController extends RestfulController{

    ClinicService clinicService
    ClinicRestService clinicRestService

    static responseFormats = ['json', 'xml']
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    ClinicController() {
        super(Clinic)
    }

    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        render JSONSerializer.setObjectListJsonResponse(clinicService.list(params)) as JSON
    }

    def show(String id) {
        render JSONSerializer.setJsonObjectResponse(clinicService.get(id)) as JSON
    }

    @Transactional
    def save() {

        Clinic clinic = new Clinic()
        def objectJSON = request.JSON
        clinic = objectJSON as Clinic

        clinic.beforeInsert()
        clinic.validate()

        if(objectJSON.id){
            clinic.id = UUID.fromString(objectJSON.id)
        }

        if (clinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinic.errors
            return
        }

        try {
            clinicService.save(clinic)
        } catch (ValidationException e) {
            respond clinic.errors
            return
        }

        respond clinic, [status: CREATED, view:"show"]
    }

    @Transactional
    def update() {

        Clinic clinic
        def objectJSON = request.JSON

        if(objectJSON.id){
            clinic = Clinic.get(objectJSON.id)
            if (clinic == null) {
                render status: NOT_FOUND
                return
            }
            clinic.properties = objectJSON
        }

        if (clinic.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond clinic.errors
            return
        }

        try {
            clinicService.save(clinic)
        } catch (ValidationException e) {
            respond clinic.errors
            return
        }

        respond clinic, [status: OK, view:"show"]
    }

    @Transactional
    def delete(Long id) {
        if (id == null || clinicService.delete(id) == null) {
            render status: NOT_FOUND
            return
        }

        render status: NO_CONTENT
    }

    def getByUUID(String uuid) {
        render JSONSerializer.setJsonObjectResponse(Clinic.findByUuid(uuid)) as JSON
    }

    def getMainClinic() {
        render JSONSerializer.setJsonObjectResponse(Clinic.findByMainClinic(true)) as JSON
    }

    def searchClinicsByDistrictId(String districtId){
        District district = District.findById(districtId)
        render JSONSerializer.setObjectListJsonResponse(Clinic.findAllByDistrictAndActive(district,true)) as JSON
        // respond communityMobilizerService.getAllByDistrictId(districtId)
    }

    def searchClinicsByProvinceCode(String provinceCode){
        Province province = Province.findByCode(provinceCode)
        render JSONSerializer.setObjectListJsonResponse(Clinic.findAllByProvinceAndActive(province,true)) as JSON
        // respond communityMobilizerService.getAllByDistrictId(districtId)
    }

    def geClinicsFromProvincialServer(int offset) {
        render JSONSerializer.setObjectListJsonResponse(clinicRestService.loadClinicsFromProvincial(offset)) as JSON
    }
}
