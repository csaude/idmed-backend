package mz.org.fgh.sifmoz.backend.migration.params.stock;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.stock.StockCenterMigrationRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class StockCenterMigrationSearchParams extends AbstractMigrationSearchParams<StockCenterMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<StockCenterMigrationRecord> doSearch(long limit) {
//        JSONArray jsonArray = getRestServiceProvider().get("/stockcenter?migration_status!=eq.\"MIGRATED\"&limit=100"); // Melhorar pra trazer apenas os nao migrados
        JSONArray jsonArray = getRestServiceProvider().get("/stockcenter?or=(migration_status.is.null,migration_status.eq.CORRECTED)&limit"+limit); // Traga apenas os nao migrados
        this.searchResults.clear();
        StockCenterMigrationRecord[] stockCenterMigrationRecords = gson.fromJson(jsonArray.toString(), StockCenterMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (stockCenterMigrationRecords != null && stockCenterMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(stockCenterMigrationRecords));
        }
        return this.searchResults;
    }
}
