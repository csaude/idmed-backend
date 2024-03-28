package mz.org.fgh.sifmoz.backend.migration.params.parameter;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.regimeTerapeutico.RegimeTerapeuticoMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.params.stock.StockMigrationSearchParams;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.List;

public class RegimeTerapeuticoMigrationSearchParams extends AbstractMigrationSearchParams<RegimeTerapeuticoMigrationRecord> {

    static Logger logger = LogManager.getLogger(StockMigrationSearchParams.class);

    @Override
    public List<RegimeTerapeuticoMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/regimeterapeutico?or=(migration_status.is.null,migration_status.eq.CORRECTED)&limit=" + limit);
        this.searchResults.clear();
        RegimeTerapeuticoMigrationRecord[] regimeTerapeuticoMigrationRecords = gson.fromJson(jsonArray.toString(), RegimeTerapeuticoMigrationRecord[].class);
        logger.info(jsonArray.toString());

        if (regimeTerapeuticoMigrationRecords != null && regimeTerapeuticoMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(regimeTerapeuticoMigrationRecords));
        }
        return this.searchResults;
    }
}
