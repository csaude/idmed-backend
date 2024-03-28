package mz.org.fgh.sifmoz.backend.migration.params.stock;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.stock.StockTakeMigrationRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockTakeMigrationSearchParams extends AbstractMigrationSearchParams<StockTakeMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockTakeMigrationSearchParams.class);
    @Override
    public List<StockTakeMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/stocktake?or=(migration_status.is.null,migration_status.eq.CORRECTED)&limit="+limit);
        this.searchResults.clear();
        StockTakeMigrationRecord[] stockTakeMigrationRecords = gson.fromJson(jsonArray.toString(), StockTakeMigrationRecord[].class);
        //List<StockTakeMigrationRecord> stockTakeMigrationRecords1 = Arrays.asList(stockTakeMigrationRecords);
        logger.info(jsonArray.toString());
        if (stockTakeMigrationRecords != null && stockTakeMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(stockTakeMigrationRecords));
        }

        return new ArrayList<>(Arrays.asList(stockTakeMigrationRecords));
    }

}
