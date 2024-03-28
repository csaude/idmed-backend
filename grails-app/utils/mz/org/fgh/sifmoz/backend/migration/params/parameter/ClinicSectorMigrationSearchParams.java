package mz.org.fgh.sifmoz.backend.migration.params.parameter;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.clinicSector.ClinicSectorMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class ClinicSectorMigrationSearchParams extends AbstractMigrationSearchParams<ClinicSectorMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<ClinicSectorMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/clinic_sector_migration_vw?limit=" + limit);
        this.searchResults.clear();
        ClinicSectorMigrationRecord[] clinicSectorMigrationRecords = gson.fromJson(jsonArray.toString(), ClinicSectorMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (clinicSectorMigrationRecords != null && clinicSectorMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(clinicSectorMigrationRecords));
        }
        return this.searchResults;
    }
}
