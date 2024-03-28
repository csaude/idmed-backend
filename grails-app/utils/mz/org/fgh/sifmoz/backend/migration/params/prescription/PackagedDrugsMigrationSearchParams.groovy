package mz.org.fgh.sifmoz.backend.migration.params.prescription

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams
import mz.org.fgh.sifmoz.backend.migration.entity.pack.PackageMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PackagedDrugsMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescriptionMigrationRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.grails.web.json.JSONArray

class PackagedDrugsMigrationSearchParams extends AbstractMigrationSearchParams<PackagedDrugsMigrationRecord> {

    static Logger logger = LogManager.getLogger(PackagedDrugsMigrationSearchParams.class)

    @Override
    List<PackagedDrugsMigrationRecord> doSearch(long limit) {

        // check first weather there is any package data to migrate
        JSONArray checkPackMigrationResults = getRestServiceProvider().get("/package_migration_vw?limit=" + limit);
        PackageMigrationRecord[] patientMigrationRecords = gson.fromJson(checkPackMigrationResults.toString(), PackageMigrationRecord[].class);
        if (patientMigrationRecords != null && patientMigrationRecords.length > 0) {
                   logger.info("PackageMigrationSearchParams.doSearch is still running, waiting for package migration to finish");
            System.out.println("PackageMigrationSearchParams.doSearch is still running, waiting for package migration to finish");
        } else {
            logger.info("PackageMigrationSearchParams.doSearch package migration finished, starting packaged drugs migration");
            System.out.println("PackagedDrugsMigrationSearchParams.doSearch total: " + limit + " Data: " + new Date());
            JSONArray jsonArray = getRestServiceProvider().get("/packaged_drugs_migration_vw?limit=" + limit)
            this.searchResults.clear()
            PackagedDrugsMigrationRecord[] packagedMigrationRecords = gson.fromJson(jsonArray.toString(), PackagedDrugsMigrationRecord[].class)
            if (packagedMigrationRecords != null && packagedMigrationRecords.length > 0) {
                this.searchResults.addAll(Arrays.asList(packagedMigrationRecords))
            }
            for (PackagedDrugsMigrationRecord record : this.searchResults) {
                record.setRestService(getRestServiceProvider())
            }
            return this.searchResults
        }
    }
}
