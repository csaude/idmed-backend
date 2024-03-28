package mz.org.fgh.sifmoz.backend.stockentrance

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.clinic.Clinic

@Transactional
@Service(StockEntrance)
abstract class StockEntranceService implements IStockEntranceService {

    def serviceMethod() {

    }

    @Override
    List<StockEntrance> getAllByClinicId(String clinicId, int offset, int max) {
        return StockEntrance.findAllByClinic(Clinic.findById(clinicId), [offset: offset, max: max])
    }

}