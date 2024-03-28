package mz.org.fgh.sifmoz.backend.migration.params.stock;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.stock.StockAdjustmentMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.entity.stock.StockTakeMigrationRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StockAdjustmentSearchParams extends AbstractMigrationSearchParams<StockAdjustmentMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockTakeMigrationSearchParams.class);
    @Override
    public List<StockAdjustmentMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/stock_adjustment_vw?limit="+limit);
        this.searchResults.clear();
        StockAdjustmentMigrationRecord[] stockAdjustmentMigrationRecords = gson.fromJson(jsonArray.toString(), StockAdjustmentMigrationRecord[].class);
       // List<StockAdjustmentMigrationRecord> stockAdjustmentMigrationRecord1 = Arrays.asList(stockAdjustmentMigrationRecords);
        logger.info(jsonArray.toString());
        if (stockAdjustmentMigrationRecords != null && stockAdjustmentMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(stockAdjustmentMigrationRecords));
        }

        return new ArrayList<>(Arrays.asList(stockAdjustmentMigrationRecords));
    }
}
