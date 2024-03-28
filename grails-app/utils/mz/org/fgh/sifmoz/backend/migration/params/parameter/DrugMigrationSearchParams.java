package mz.org.fgh.sifmoz.backend.migration.params.parameter;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.drug.DrugMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class DrugMigrationSearchParams extends AbstractMigrationSearchParams<DrugMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<DrugMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/drug_migration_vw?limit=" + limit);
        this.searchResults.clear();
        DrugMigrationRecord[] drugMigrationRecords = gson.fromJson(jsonArray.toString(), DrugMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (drugMigrationRecords != null && drugMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(drugMigrationRecords));
        }
        return this.searchResults;
    }
}
