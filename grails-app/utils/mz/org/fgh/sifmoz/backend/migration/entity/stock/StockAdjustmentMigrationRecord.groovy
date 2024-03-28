package mz.org.fgh.sifmoz.backend.migration.entity.stock

import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.migration.base.log.AbstractMigrationLog
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migration.common.MigrationError
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.stock.Stock
import mz.org.fgh.sifmoz.backend.stockadjustment.InventoryStockAdjustment
import mz.org.fgh.sifmoz.backend.stockadjustment.StockAdjustment
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory
import mz.org.fgh.sifmoz.backend.stockoperation.StockOperationType
import mz.org.fgh.sifmoz.backend.utilities.Utilities


class StockAdjustmentMigrationRecord extends AbstractMigrationRecord {
    private Integer id

   // private StockMigrationRecord stock

    private Date capturedate

    private int stockcount

    private int adjustedvalue

    private String notes

    private String stock_id

    private String stocktake_id

    private String clinicuuid

   // private StockTakeMigrationRecord stockTake

    private String finalised

    //-----------------------------------------------------

    @Override
    List<MigrationLog> migrate() {

        List<MigrationLog> logs = new ArrayList<>();
        InventoryStockAdjustment.withTransaction {
            getMigratedRecord().setCaptureDate(this.capturedate)
           // getMigratedRecord().setStockTake()
            getMigratedRecord().setNotes(this.notes)
            getMigratedRecord().setBalance(this.stockcount)
            getMigratedRecord().setFinalised(true)

            StockOperationType operationType;
            if (this.adjustedvalue > 0) {
                operationType =  StockOperationType.findByCode("AJUSTE_POSETIVO")
            } else  if (this.adjustedvalue < 0)  {
                operationType =  StockOperationType.findByCode("AJUSTE_NEGATIVO")
            } else {
                operationType =  StockOperationType.findByCode("SEM_AJUSTE")
            }

            getMigratedRecord().setAdjustedValue(Math.abs(this.adjustedvalue))

            getMigratedRecord().setOperation(operationType)

            def clinic = Clinic.findWhere(mainClinic: true)

            if (clinic != null) {
                getMigratedRecord().setClinic(clinic)
            } else {
                String[] msg = [String.format(MigrationError.CLINIC_NOT_FOUND.getDescription())]
                logs.add(new MigrationLog(MigrationError.MAIN_CLINIC_NOT_IDENTIFIED.getCode(),msg,getId(), getEntityName()))
            }

         MigrationLog migrationLogStock =  MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(String.valueOf(this.stock_id) as int,"stock" )
            if(migrationLogStock != null) {
                Stock stock = Stock.findById(migrationLogStock.iDMEDId)
                getMigratedRecord().setAdjustedStock(stock)
            } else {
                String[] msg = [String.format(MigrationError.STOCK_NOT_FOUND.getDescription())]
                logs.add(new MigrationLog(MigrationError.STOCK_NOT_FOUND.getCode(),msg,getId(), getEntityName()))
            }

            MigrationLog migrationLogInventory =  MigrationLog.findBySourceIdAndSourceEntityAndIDMEDIdIsNotNull(this.stocktake_id as int,"stocktake" )
            if(migrationLogStock != null) {
                Inventory inventory = Inventory.findById(migrationLogInventory.iDMEDId)
                getMigratedRecord().setInventory(inventory)
            }
            else {
                String[] msg = [String.format(MigrationError.INVENTORY_NOT_FOUND.getDescription())]
                logs.add(new MigrationLog(MigrationError.INVENTORY_NOT_FOUND.getCode(),msg,getId(), getEntityName()))
            }
            if (Utilities.listHasElements(logs)) return logs

            getMigratedRecord().validate()
            if(getMigratedRecord().hasErrors()) {
                logs.addAll(generateUnknowMigrationLog(this, getMigratedRecord().getErrors().toString()))
            } else {
                if (getMigratedRecord().id == null)  getMigratedRecord().setId(UUID.randomUUID().toString())
                getMigratedRecord().save(flush:true)
            }

        }
        return logs;
    }

    @Override
    void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        this.id
    }

    @Override
    String getEntityName() {
        return "stockadjustment";
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new InventoryStockAdjustment()  ;
    }

    @Override
    InventoryStockAdjustment getMigratedRecord() {
        return (InventoryStockAdjustment) super.getMigratedRecord();
    }
}
