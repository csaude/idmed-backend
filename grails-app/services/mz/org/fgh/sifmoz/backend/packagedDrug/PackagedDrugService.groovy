package mz.org.fgh.sifmoz.backend.packagedDrug

import grails.gorm.services.Service
import grails.gorm.transactions.Transactional
import mz.org.fgh.sifmoz.backend.packaging.Pack
import mz.org.fgh.sifmoz.backend.reports.monitoringAndEvaluation.DrugQuantityTemp

@Transactional
@Service(PackagedDrug)
abstract class PackagedDrugService implements IPackagedDrugService{

    @Override
    List<PackagedDrug> getAllByPackId(String packId) {
        return PackagedDrug.findAllByPack(Pack.findById(packId))
    }


}
