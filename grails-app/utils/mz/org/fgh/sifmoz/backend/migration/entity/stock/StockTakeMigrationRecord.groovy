package mz.org.fgh.sifmoz.backend.migration.entity.stock

import mz.org.fgh.sifmoz.backend.clinic.Clinic;
import mz.org.fgh.sifmoz.backend.migration.base.log.AbstractMigrationLog
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigrationRecord
import mz.org.fgh.sifmoz.backend.migration.common.MigrationError
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.stockinventory.Inventory
import mz.org.fgh.sifmoz.backend.utilities.Utilities

public class StockTakeMigrationRecord extends AbstractMigrationRecord {
    private Integer id

    private String stocktakenumber

    private Date startdate

    private Date enddate

    private Set<StockAdjustmentMigrationRecord> adjustments

    private boolean open

    //---------------------------------------------------

    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>();
        Inventory.withTransaction {
            def clinic = Clinic.findWhere(mainClinic: true)

            if (clinic != null) {
                getMigratedRecord().setClinic(clinic)
            } else {
                String[] msg = [String.format(MigrationError.MAIN_CLINIC_NOT_IDENTIFIED.getDescription())]
                logs.add(new MigrationLog(MigrationError.MAIN_CLINIC_NOT_IDENTIFIED.getCode(),msg,getId(), getEntityName()))
            }

            getMigratedRecord().setStartDate(this.startdate)
            getMigratedRecord().setEndDate(this.enddate)
            getMigratedRecord().setClinic(clinic)
            getMigratedRecord().setGeneric(true)
            getMigratedRecord().setSequence(0)
            if (Utilities.listHasElements(logs)) return logs

            getMigratedRecord().validate()
            if(getMigratedRecord().hasErrors()) {
                logs.addAll(generateUnknowMigrationLog(this, getMigratedRecord().getErrors().toString()))
            }
            else {
                if (getMigratedRecord().id == null)  getMigratedRecord().setId(UUID.randomUUID().toString())
                def createdInventory = getMigratedRecord().save(flush: true)
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
        return "stocktake";
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new Inventory();
    }

    @Override
    Inventory getMigratedRecord() {
        return (Inventory) super.getMigratedRecord();
    }
}
