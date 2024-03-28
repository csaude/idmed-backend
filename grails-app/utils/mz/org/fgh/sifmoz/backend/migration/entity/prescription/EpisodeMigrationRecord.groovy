package mz.org.fgh.sifmoz.backend.migration.entity.prescription

import mz.org.fgh.sifmoz.backend.episode.Episode
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog

public class EpisodeMigrationRecord extends AbstractMigrationRecord {

    private Integer id;

    void setId(Integer id) {
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
        return "Episode";
    }


    @Override
    public MigratedRecord initMigratedRecord() {
        return new Episode();
    }


    @Override
    Episode getMigratedRecord() {
        return (Episode) super.getMigratedRecord()
    }

}
