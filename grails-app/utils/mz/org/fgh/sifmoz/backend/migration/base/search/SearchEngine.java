package mz.org.fgh.sifmoz.backend.migration.base.search;

import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;

import java.util.List;

public class SearchEngine<T extends AbstractMigrationRecord> {

    public static final long RECORDS_PER_SEARCH = 1000;

    public static final String PREPARING_SEARCH = "PREPARING_SEARCH";

    public static final String PERFORMING_SEARCH = "PERFORMING_SEARCH";

    public static final String LOADING_MORE_RECORDS = "LOADING_MORE_RECORDS";

    public static final String SEARCH_FINISHED = "SEARCH_FINISHED";

    protected AbstractMigrationSearchParams<T> searchParams;

    public SearchEngine(AbstractMigrationSearchParams<T> searchParams) {
        this.searchParams = searchParams;
    }

    public List<T> doSearch() {
        return searchParams.doSearch(RECORDS_PER_SEARCH);
    }

    public AbstractMigrationSearchParams<T> getSearchParams() {
        return searchParams;
    }
}
