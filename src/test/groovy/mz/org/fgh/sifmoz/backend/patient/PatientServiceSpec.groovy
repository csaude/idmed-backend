package mz.org.fgh.sifmoz.backend.patient

import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import jakarta.inject.Inject
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.DistrictService
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.ProvinceService
import mz.org.fgh.sifmoz.backend.patientIdentifier.PatientServiceIdentifierService
import org.hibernate.SessionFactory
import spock.lang.Specification


class PatientServiceSpec extends Specification implements ServiceUnitTest<PatientService> ,DataTest {

    //IPatientService patientService
    DistrictService districtService
    ProvinceService provinceService
    PatientServiceIdentifierService patientServiceIdentifierService
    IPatientService patientService
    SessionFactory sessionFactory
    void setup() {
        patientService = Mock(IPatientService)
        // Initialize mocks
       // patientService = Mock(IPatientService)
        districtService = Mock(DistrictService)
        provinceService = Mock(ProvinceService)
        patientServiceIdentifierService = Mock(PatientServiceIdentifierService)
      //  bookService = new IPatientService()
       //   mockDomain(Patient)
       // controller.patientService = patientService
//        service.districtService = districtService
  //      service.provinceService = provinceService
  //      service.patientServiceIdentifierService = patientServiceIdentifierService

      //  sessionFactory = Mock(SessionFactory)
      //  service.sessionFactory = sessionFactory
    }
    void setupSpec() {
        // Mock the Patient domain class to enable GORM functionality in unit tests
        mockDomain(Patient)
    }
     void "test something"() {
        expect:
        service.doSomething()
     }

    void "test saving a patient in a unit test"() {
        given: "A new patient"
        def patient = new Patient(firstNames: "John", middleNames: "John",lastNames: "Doe",
                gender: 'M')

        when: "The patient is saved"
      patientService.save(patient) // Call the mocked service method

        then: "The patient should be persisted"
        1 * patientService.save(_) // Verify the service's save method was called once
    }
}
