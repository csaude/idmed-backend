package mz.org.fgh.sifmoz.backend.migration.params.parameter;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.clinic.ClinicMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class ClinicMigrationSearchParams extends AbstractMigrationSearchParams<ClinicMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<ClinicMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/clinic?or=(migration_status.is.null,migration_status.eq.CORRECTED)&limit=" + limit);
        this.searchResults.clear();
        ClinicMigrationRecord[] clinicMigrationRecords = gson.fromJson(jsonArray.toString(), ClinicMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (clinicMigrationRecords != null && clinicMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(clinicMigrationRecords));
        }
        return this.searchResults;
    }
}
