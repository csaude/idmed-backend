package mz.org.fgh.sifmoz.backend.migration.entity.stock

import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migration.common.MigrationError
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.stockcenter.StockCenter

class StockCenterMigrationRecord extends AbstractMigrationRecord {
    private Integer id

    private String stockcentername

    private boolean preferred

    private Set<StockMigrationRecord> stockMigrationRecords

    private String clinicuuid

    private boolean migration_status

    //------------------------------------------------

    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        StockCenter.withTransaction {
            getMigratedRecord().setName(this.stockcentername)
            getMigratedRecord().setCode(this.stockcentername.replaceAll(" ", "_").toUpperCase())
            getMigratedRecord().setPrefered(this.preferred)
            Clinic clinicAux = Clinic.findByUuid(this.clinicuuid)

            getMigratedRecord().setClinic(this.preferred ? Clinic.findByMainClinic(true) : clinicAux);

            if (getMigratedRecord().getClinic() == null)  {
                String[] msg = [String.format(MigrationError.CLINIC_NOT_FOUND.getDescription(), this.clinicuuid)]
                logs.add(new MigrationLog(MigrationError.CLINIC_NOT_FOUND.getCode(),msg,getId(), getEntityName()));
            }

            if (getMigratedRecord().hasErrors()) {
                logs.addAll(generateUnknowMigrationLog(this, getMigratedRecord().getErrors().toString()))
            }

            if (logs.size() == 0){
                getMigratedRecord().setId(UUID.randomUUID().toString())
                getMigratedRecord().save(flush: true)
            }

        }
        return logs
    }

    @Override
    void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        return this.id
    }

    @Override
    String getEntityName() {
        return "stockcenter"
    }
    @Override
    MigratedRecord initMigratedRecord() {
        return new StockCenter()
    }

    @Override
    StockCenter getMigratedRecord() {
        return (StockCenter) super.getMigratedRecord()
    }
}
