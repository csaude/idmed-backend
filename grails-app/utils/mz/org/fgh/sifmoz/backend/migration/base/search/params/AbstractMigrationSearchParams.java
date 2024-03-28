package mz.org.fgh.sifmoz.backend.migration.base.search.params;

import com.google.gson.Gson;
import mz.org.fgh.sifmoz.backend.restUtils.RestService;
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMigrationSearchParams<T extends AbstractMigrationRecord> {
    public static final String MIGRATION_STATUS_MIGRATED = "MIGRATED";
    public static final String MIGRATION_STATUS_REJECTED = "REJECTED";
    public static final String MIGRATION_STATUS_CORRECTED = "CORRECTED";

    protected List<T> searchResults;
    protected String migrationStatus;
    protected String searchCondition;
    protected String migrationUrl;
    protected RestService restServiceProvider;
    protected Gson gson;


    public AbstractMigrationSearchParams() {
        this.searchResults = new ArrayList<>();
        this.gson = new Gson();
        this.restServiceProvider = new RestService("MIGRATION", "IDART");
    }

    public abstract List<T> doSearch(long limit);

    public RestService getRestServiceProvider() {
        return restServiceProvider;
    }

    public Gson getGson() {
        return gson;
    }
}
