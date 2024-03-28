package mz.org.fgh.sifmoz.backend.migration.params.parameter;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.doctor.DoctorMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class DoctorMigrationSearchParams extends AbstractMigrationSearchParams<DoctorMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<DoctorMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/doctor?or=(migration_status.is.null,migration_status.eq.CORRECTED)&limit=" + limit);
        this.searchResults.clear();
        DoctorMigrationRecord[] doctorMigrationRecords = gson.fromJson(jsonArray.toString(), DoctorMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (doctorMigrationRecords != null && doctorMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(doctorMigrationRecords));
        }
        return this.searchResults;
    }
}
