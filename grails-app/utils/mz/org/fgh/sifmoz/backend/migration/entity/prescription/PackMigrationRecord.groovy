package mz.org.fgh.sifmoz.backend.migration.entity.prescription

import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.packaging.Pack

class PackMigrationRecord extends AbstractMigrationRecord {


    private Integer id;


    public void setId(Integer id) {
        this.id = id;
    }


    @Override
    public List<MigrationLog> migrate() {
        return null;
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
        return "Package";
    }


    @Override
    public MigratedRecord initMigratedRecord() {
        return new Pack()
    }

    @Override
    Pack getMigratedRecord() {
        return (Pack) super.getMigratedRecord()
    }

}
