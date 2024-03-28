package mz.org.fgh.sifmoz.backend.migration.params.patient;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.patient.PatientMigrationRecord;
import mz.org.fgh.sifmoz.backend.reports.dashboard.DashBoardController;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PatientMigrationSearchParams extends AbstractMigrationSearchParams<PatientMigrationRecord> {


    static Logger logger = LogManager.getLogger(PatientMigrationSearchParams.class);
    @Override
    public List<PatientMigrationRecord> doSearch(long limit) {
        JSONArray jsonArray = getRestServiceProvider().get("/patient_migration_vw?limit="+limit);
        this.searchResults.clear();
        PatientMigrationRecord[] patientMigrationRecords = gson.fromJson(jsonArray.toString(), PatientMigrationRecord[].class);
        if (patientMigrationRecords != null && patientMigrationRecords.length > 0) {
            this.searchResults.addAll(Arrays.asList(patientMigrationRecords));
        }
        return this.searchResults;
    }
}
