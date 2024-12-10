package mz.org.fgh.sifmoz.backend.patient

import grails.testing.gorm.DomainUnitTest
import grails.testing.web.controllers.ControllerUnitTest
import grails.validation.ValidationException
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.DistrictService
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.LocalidadeService
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.ProvinceService
import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.healthInformationSystem.SystemConfigs
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifier
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifierService
import mz.org.fgh.sifmoz.backend.patientVisit.PatientVisit
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import spock.lang.Specification

class PatientControllerSpec extends Specification implements ControllerUnitTest<PatientController>, DomainUnitTest<Patient> {

    IPatientService patientService
    DistrictService districtService
    ProvinceService provinceService
    PatientServiceIdentifierService patientServiceIdentifierService

    void setup() {
        // Initialize mocks
        patientService = Mock(IPatientService)
        districtService = Mock(DistrictService)
        provinceService = Mock(ProvinceService)
        patientServiceIdentifierService = Mock(PatientServiceIdentifierService)
        mockDomain(PatientVisit)
      //  mockDomain(PatientServiceIdentifier)
        controller.patientService = patientService
//        controller.districtService = districtService
   //     controller.provinceService = provinceService
  //      controller.patientServiceIdentifierService = patientServiceIdentifierService
    }

    void "test save patient"() {
        given: "a valid patient JSON"
        def patientJson = [
                firstNames  : "John",
                middleNames : "John",
                lastNames   : "Doe",
                gender      : 'M',
                province : [id: '1'],
                district : [id: '1']
        ]

        request.JSON = patientJson

        and: "mock clinic and other necessary responses"
        def mockClinic = Mock(Clinic)
      //  def mockSystemConfigs = Mock(SystemConfigs)
        def mockSystemConfigs = new SystemConfigs(key: "INSTALATION_TYPE", value: "PROVINCIAL")
        Clinic.metaClass.static.findByMainClinic = { boolean value -> mockClinic }
        SystemConfigs.metaClass.static.findByKey= { String key ->
            key == "INSTALATION_TYPE" ? mockSystemConfigs : null }

        and: "mock the behavior of patient service"
        patientService.save(_) >> { Patient patient ->
            patient.id = UUID.randomUUID() // Assign a random UUID to mock a saved entity
            return patient
        }

        when: "the save action is called"
        request.method = 'POST'
        controller.save()

        then: "the patient service's save method should be called once"
        1 * patientService.save(_)

        and: "the response should contain the created patient"
        response.json.firstNames == "John"
        response.json.lastNames == "Doe"
        response.status == 200
    }

    void "test delete patient when ID is null"() {
        when: "The delete action is called with a null id"
        request.method = 'DELETE'
        controller.delete(null)

        then: "A NOT_FOUND response is returned"
        response.status == 404
    }

    void "test delete patient when patient does not exist"() {
        given: "A non-existent patient ID"
        def patientId = 999L
        request.method = 'DELETE'
        and: "The patientService returns null for delete"
        patientService.delete(patientId) >> null

        when: "The delete action is called"
        controller.delete(patientId)

        then: "A NOT_FOUND response is returned"
        response.status == 404
    }


    void "test delete patient when patient exists"() {
        given: "An existing patient ID"
        def patientId = 123L
        request.method = 'DELETE'
        and: "The patientService successfully deletes the patient"
        patientService.delete(patientId) >> new Patient()

        when: "The delete action is called"
        controller.delete(patientId)

        then: "A NO_CONTENT response is returned"
        response.status == 204
    }

    void "test show patient when patient exists"() {
        given: "An existing patient ID"
        def patientId = "123"
        def patient = new Patient(firstNames: "John1", lastNames: "Doe1")
        request.method = 'GET'
        and: "The patientService returns the patient"
        patientService.get(patientId) >> patient

        when: "The show action is called"
        controller.show(patientId)

        then: "The response contains the patient in JSON format"
        response.status == 200
        response.json.firstNames == "John1"
        response.json.lastNames == "Doe1"
    }

    void "test index with default max value"() {
        given: "No max parameter is provided"
       // def params = [:]  // Simulating request params without 'max'

        and: "The patientService returns a list of patients"
        def patientList = [new Patient(firstNames: "John", lastNames: "Doe")]
        patientService.list(_) >> patientList
        request.method = 'GET'
        when: "The index action is called"
        controller.index(null)  // No max value passed

        then: "The params.max should default to 10"
        // params.max == 10

        and: "The response contains the patient list in JSON format"
        response.status == 200
        response.json.size() == 1
        response.json[0].firstNames == "John"
        response.json[0].lastNames == "Doe"
    }

    void "test searchByParam method"() {
        given: "A search string and clinic ID"
        String searchString = "12345-12"
        String clinic_Id = "1"

        and: "Mock patient list returned by patientService"

        when: "The searchByParam method is called"
        request.method = 'GET'
        patientService.search(_, _) >> { String search, String clinicId ->
            return [
                    new Patient(id: '1', firstNames: "John", lastNames: "Doe", gender: "M", matchId : 1,identifiers: [new PatientServiceIdentifier(id: 1, identifierType: "National ID", value: "12345/12")],clinic : [id: '1'],province : [id: '1'],
                            district : [id: '1']),
                    new Patient(id: '2', firstNames: "Jane", lastNames: "Doe", gender: "F", matchId : 2,identifiers: [new PatientServiceIdentifier(id: 1, identifierType: "National ID", value: "1234566/12")],clinic : [id: '1'],province : [id: '1'],
                            district : [id: '1'])
            ]
        }
        controller.searchByParam(searchString, clinic_Id)


        then: "The correct JSON response is rendered"
        // def response = response.text as JSON
        response.status == 200
        def jsonResponse = response.json
        jsonResponse.size() == 2
        jsonResponse[0].firstNames == "John"
        jsonResponse[1].firstNames == "Jane"
    }

    void "test countPatientSearchResult method"() {
        given: "A patient search request with a JSON object"
        def patientSearchJSON = [
                firstNames: "John",
                lastNames: "Doe",
                gender: "M"
        ]
        request.JSON = patientSearchJSON

        and: "A mock count value returned by patientService"
        patientService.countPatientSearchResult(_) >> 5  // Mocking the return value to be 5

        when: "The countPatientSearchResult method is called"
        controller.countPatientSearchResult()

        then: "The correct count is rendered as a response"
        response.text == "5"  // Assuming the response is just the count as a string
        response.status == 200  // Assuming a successful response status
    }

    void "test getByClinicId method"() {
        given: "A clinic ID and pagination parameters"
        String clinicId = "1"
        int offset = 0
        int max = 10

        and: "A mock patient list returned by patientService"
        def patientList = [
                new Patient(id: '1', firstNames: "John", lastNames: "Doe", gender: "M", identifiers: [new PatientServiceIdentifier(id: 1, identifierType: "National ID", value: "12345")], clinic: [id: clinicId]),
                new Patient(id: '2', firstNames: "Jane", lastNames: "Doe", gender: "F", identifiers: [new PatientServiceIdentifier(id: 2, identifierType: "National ID", value: "67890")], clinic: [id: clinicId])
        ]

        patientService.getAllByClinicId(clinicId, offset, max) >> patientList // Mocking the service method

        when: "The getByClinicId method is called"
        controller.getByClinicId(clinicId, offset, max)

        then: "The correct JSON response is rendered"
        response.status == 200 // Assuming a successful response status
        def jsonResponse = response.json
        jsonResponse.size() == 2 // Check the size of the returned list
        jsonResponse[0].firstNames == "John" // Check the first patient's name
        jsonResponse[1].firstNames == "Jane" // Check the second patient's name
    }

    /*
    void "test mergeUnitePatients method"() {
        given: "Two patient IDs to hold and delete"
        String patientToHoldId = "holdPatientId"
        String patientToDeleteId = "deletePatientId"

        and: "Mock patients returned by Patient domain"
        def patientToHold = new Patient( firstNames: "John", lastNames: "Doe", origin: "Clinic A").save(flush: true)
        def patientToDelete = new Patient(firstNames: "Jane", lastNames: "Doe", origin: "Clinic B").save(flush: true)

        and: "Mock PatientServiceIdentifier objects"
        def serviceIdentifierHold = new PatientServiceIdentifier(patient: patientToHold, service: new ClinicalService(code: "Service1")).save(flush: true)
        def serviceIdentifierDelete = new PatientServiceIdentifier(patient: patientToDelete, service: new ClinicalService(code: "Service1")).save(flush: true)

        // Setting up the behavior for PatientServiceIdentifier mock
        GroovyMock(PatientServiceIdentifier, global: true)
        PatientServiceIdentifier.findAllByPatient(patientToHold) >> [serviceIdentifierHold]
        PatientServiceIdentifier.findAllByPatient(patientToDelete) >> [serviceIdentifierDelete]

        def patientVisit1 = new PatientVisit(patient: patientToHold, visitDate: new Date()).save(flush: true)
        def patientVisit2 = new PatientVisit(patient: patientToDelete, visitDate: new Date()).save(flush: true)

        // Mock the behavior of findAllByPatient for PatientVisit
        GroovyMock(PatientVisit, global: true)
        PatientVisit.findAllByPatient(patientToHold) >> [patientVisit1]
        PatientVisit.findAllByPatient(patientToDelete) >> [patientVisit2]

        and: "Mock episodes associated with the patient service identifiers"
     //   GroovyMock(Episode, global: true)
     //   def episode = new Episode(patientServiceIdentifier: serviceIdentifierDelete).save(flush: true)

        when: "The mergeUnitePatients method is called"
        controller.mergeUnitePatients(patientToHoldId, patientToDeleteId)

        then: "The patient service identifiers are merged correctly"
        1 * PatientServiceIdentifier.withTransaction(_)
        1 * Episode.withTransaction(_)

        and: "The patient to delete is deleted"
        !Patient.exists(patientToDeleteId) // Ensure the patient to delete no longer exists

        and: "The associated episodes are updated to hold patient"
        episode.patientServiceIdentifier == serviceIdentifierHold // Ensure the episode's patient service identifier is updated
    }



    void "test update method"() {
        given: "A valid patient ID and a patient saved in the database"
        String patientToHoldId = "holdPatientId"
        def existingPatient = new Patient(id: patientToHoldId, firstNames: "Old Name").save(flush: true) // Ensure patient is saved
        GroovyMock(Patient, global: true)
        Patient.get(patientToHoldId) >> existingPatient
        and: "A valid JSON request to update the patient"
        def objectJSON = [
                id: patientToHoldId, // Use the same ID as saved
                firstNames: "John",
                lastNames: "Doe",
                gender: "M",
                dateOfBirth: new Date(),
                cellphone: "1234567890",
                alternativeCellphone: "0987654321",
                address: "123 Main St",
                province: "SomeProvince",
                bairro: [code: "SomeBairro"],
                district: "SomeDistrict",
                postoAdministrativo: "SomePosto",
                hisUuid: "UUID-12345"
        ]
        request.method = 'PUT'
        request.JSON = objectJSON // Simulate the incoming JSON request

        when: "The update method is called"
        controller.update()

        then: "The patient is updated successfully"
        response.status == 200 // Assert the status is OK
        def updatedPatient = Patient.get(patientToHoldId) // Retrieve the updated patient
        updatedPatient.firstNames == "John" // Assert the name was updated
        updatedPatient.lastNames == "Doe" // Assert the last name was updated
    }

     */
}
