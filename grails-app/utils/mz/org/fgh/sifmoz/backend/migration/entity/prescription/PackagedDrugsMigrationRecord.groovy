package mz.org.fgh.sifmoz.backend.migration.entity.prescription

import mz.org.fgh.sifmoz.backend.drug.Drug;
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog;
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrug
import mz.org.fgh.sifmoz.backend.packagedDrug.PackagedDrugStock
import mz.org.fgh.sifmoz.backend.packaging.Pack;
import mz.org.fgh.sifmoz.backend.prescription.Prescription
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.utilities.Utilities;

import java.util.List;

public class PackagedDrugsMigrationRecord extends AbstractMigrationRecord {

    private Integer id;


    private Integer amount;

    private Integer stock;

    private Integer parentpackage;

    private char modified;

    private double packageddrugsindex;

    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    public List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        PackagedDrug.withTransaction {
            MigrationLog stockMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.stock, "stock")
            if (stockMigrationLog == null) throw new RuntimeException("Não foi encontrado o log do respectivo registo stock.")
            Stock stock = Stock.findById(stockMigrationLog.getiDMEDId())

            MigrationLog packMigrationLog = MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.parentpackage, "package")
            if (packMigrationLog == null) throw new RuntimeException("Não foi encontrado o log do respectivo registo package.")
            Pack pack = Pack.findById(packMigrationLog.getiDMEDId())

            if (pack == null) throw new RuntimeException("Não foi encontrado o respectivo registo PACK.")

            PackagedDrug packagedDrug = getMigratedRecord() as PackagedDrug
            packagedDrug.setQuantitySupplied(this.amount / stock.getDrug().getPackSize())
            packagedDrug.setNextPickUpDate(pack.getNextPickUpDate())
            packagedDrug.setDrug(stock.getDrug())
            packagedDrug.setCreationDate(new Date())
            packagedDrug.setPack(pack)
            packagedDrug.setToContinue(true)
            packagedDrug.setPackagedDrugStocks(new HashSet<PackagedDrugStock>())

            //PackageDrugStock
            PackagedDrugStock packagedDrugStock = new PackagedDrugStock()
            packagedDrugStock.setQuantitySupplied(this.amount / stock.getDrug().getPackSize())
            packagedDrugStock.setDrug(stock.getDrug())
            packagedDrugStock.setStock(stock)
            packagedDrugStock.setCreationDate(new Date())
            packagedDrugStock.setPackagedDrug(packagedDrug)
            packagedDrugStock.setId(UUID.randomUUID().toString())
            packagedDrug.getPackagedDrugStocks().add(packagedDrugStock)
            packagedDrug.validate()

            if (!packagedDrug.hasErrors()) {
                packagedDrug.setId(UUID.randomUUID().toString())
                packagedDrug.save(flush: true)
            } else {
                logs.addAll(generateUnknowMigrationLog(this, packagedDrug.getErrors().toString()))
                return logs
            }

        }
        return logs

    }

    @Override
    public void updateIDMEDInfo() {

    }

    @Override
    public int getId() {
        return this.id;
    }


    @Override
    public String getEntityName() {
        return "packageddrugs";
    }

    @Override
    public MigratedRecord initMigratedRecord() {
        return new PackagedDrug();
    }
}
