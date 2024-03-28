package mz.org.fgh.sifmoz.backend.migration.params.prescription

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams
import mz.org.fgh.sifmoz.backend.migration.entity.patient.PatientMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescribedDrugsMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescriptionMigrationRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.grails.web.json.JSONArray

class PrescribedDrugsMigrationSearchParams extends AbstractMigrationSearchParams<PrescribedDrugsMigrationRecord> {

    static Logger logger = LogManager.getLogger(PrescribedDrugsMigrationSearchParams.class)

    @Override
    List<PrescribedDrugsMigrationRecord> doSearch(long limit) {

        // check first weather there is any patient data to migrate
        JSONArray checkPrescriptionMigrationResults = getRestServiceProvider().get("/prescription_migration_vw?limit=" + limit)
        PrescriptionMigrationRecord[] prescriptionMigrationRecords = gson.fromJson(checkPrescriptionMigrationResults.toString(), PrescriptionMigrationRecord[].class)
        if (prescriptionMigrationRecords != null && prescriptionMigrationRecords.length > 0) {
            logger.info("PrescriptionMigrationSearchParams.doSearch is still running, waiting for prescription migration to finish")
                println("PrescriptionMigrationSearchParams.doSearch is still running, waiting for prescription migration to finish")
        } else {
            logger.info("PrescriptionMigrationSearchParams.doSearch prescription migration finished, starting prescribed drugs migration")
            println("PrescribedDrugsMigrationSearchParams.doSearch total: " + limit + " Data: " + new Date())
            JSONArray jsonArray = getRestServiceProvider().get("/prescribed_drugs_migration_vw?limit=" + limit)
            this.searchResults.clear()
            PrescribedDrugsMigrationRecord[] prescribedMigrationRecords = gson.fromJson(jsonArray.toString(), PrescribedDrugsMigrationRecord[].class)
            if (prescribedMigrationRecords != null && prescribedMigrationRecords.length > 0) {
                this.searchResults.addAll(Arrays.asList(prescribedMigrationRecords))
            }
            for (PrescribedDrugsMigrationRecord record : this.searchResults) {
                record.setRestService(getRestServiceProvider())
            }
            return this.searchResults
        }
    }
}
