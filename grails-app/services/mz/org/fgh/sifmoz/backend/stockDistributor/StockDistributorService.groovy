package mz.org.fgh.sifmoz.backend.stockDistributor

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.stockentrance.StockEntrance

@Transactional
@Service(StockDistributor)
abstract class StockDistributorService implements IStockDistributorService {

    def serviceMethod() {

    }

    @Override
    List<StockDistributor> getAllByClinicId(String clinicId, int offset, int max) {
        return StockDistributor.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

}