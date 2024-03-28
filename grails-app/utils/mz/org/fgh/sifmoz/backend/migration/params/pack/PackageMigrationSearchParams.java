package mz.org.fgh.sifmoz.backend.migration.params.pack;

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams;
import mz.org.fgh.sifmoz.backend.migration.entity.pack.PackageMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.entity.patient.PatientMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescribedDrugsMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescriptionMigrationRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.grails.web.json.JSONArray;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PackageMigrationSearchParams extends AbstractMigrationSearchParams<PackageMigrationRecord> {
    static Logger logger = LogManager.getLogger(PackageMigrationSearchParams.class);

    @Override
    public List<PackageMigrationRecord> doSearch(long limit) {

        // check first weather there is any prescribed drugs data to migrate
        JSONArray checkPrescribedDrugsnMigrationResults = getRestServiceProvider().get("/prescribed_drugs_migration_vw?limit=" + limit);
        PrescribedDrugsMigrationRecord[] prescribedMigrationRecords = gson.fromJson(checkPrescribedDrugsnMigrationResults.toString(), PrescribedDrugsMigrationRecord[].class);
        if (prescribedMigrationRecords != null && prescribedMigrationRecords.length > 0) {
                   logger.info("PrescribeDrugsMigrationSearchParams.doSearch is still running, waiting for prescribedDrugs migration to finish");
            System.out.println("PrescribeDrugsMigrationSearchParams.doSearch is still running, waiting for prescribedDrugs migration to finish");
        } else {
            logger.info("PrescribeDrugsMigrationSearchParams.doSearch is done, starting package migration");
            System.out.println("PackageMigrationSearchParams.doSearch total: " + limit + " Data: " + new Date());
            JSONArray jsonArray = getRestServiceProvider().get("/package_migration_vw?limit=" + limit);
//            if (jsonArray == null) {
//                try {
//                    Thread.sleep(3000);
////                Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                jsonArray = getRestServiceProvider().get("/package_migration_vw?limit=" + limit);
//            }
            this.searchResults.clear();
            PackageMigrationRecord[] patientMigrationRecords = gson.fromJson(jsonArray.toString(), PackageMigrationRecord[].class);
            if (patientMigrationRecords != null && patientMigrationRecords.length > 0) {
                this.searchResults.addAll(Arrays.asList(patientMigrationRecords));
            }
        }
            return this.searchResults;
    }
}
