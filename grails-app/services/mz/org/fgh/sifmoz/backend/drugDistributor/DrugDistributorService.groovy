package mz.org.fgh.sifmoz.backend.drugDistributor

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic

@Transactional
@Service(DrugDistributor)
abstract class DrugDistributorService implements  IDrugDistributorService{
 

    @Override
    List<DrugDistributor> getAllByClinicId(String clinicId, int offset, int max) {
        return DrugDistributor.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }
}
