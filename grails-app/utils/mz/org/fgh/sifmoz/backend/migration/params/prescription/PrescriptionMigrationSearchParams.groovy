package mz.org.fgh.sifmoz.backend.migration.params.prescription

import mz.org.fgh.sifmoz.backend.migration.base.search.params.AbstractMigrationSearchParams
import mz.org.fgh.sifmoz.backend.migration.entity.patient.PatientMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.entity.prescription.PrescriptionMigrationRecord
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.grails.web.json.JSONArray

class PrescriptionMigrationSearchParams extends AbstractMigrationSearchParams<PrescriptionMigrationRecord> {

    static Logger logger = LogManager.getLogger(PrescriptionMigrationSearchParams.class)

    @Override
    List<PrescriptionMigrationRecord> doSearch(long limit) {
        // check first weather there is any patient data to migrate
        JSONArray checkPatientMigrationResults = getRestServiceProvider().get("/patient_migration_vw?limit=" + limit);
        PatientMigrationRecord[] patientMigrationRecords = gson.fromJson(checkPatientMigrationResults.toString(), PatientMigrationRecord[].class);
        if (patientMigrationRecords != null && patientMigrationRecords.length > 0) {
            logger.info("PatientMigrationSearchParams.doSearch is still running, waiting for patient migration to finish");
                println("PatientMigrationSearchParams.doSearch is still running, waiting for patient migration to finish");
        } else {
            logger.info("PatientMigrationSearchParams.doSearch patient migration finished, starting prescription migration");
            println("PrescriptionMigrationSearchParams.doSearch total :  " + limit +" Data :  " + new Date());
            JSONArray jsonArray = getRestServiceProvider().get("/prescription_migration_vw?limit=" + limit)
//            if (jsonArray == null) {
//                try {
//                    Thread.sleep(3000);
////                Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                jsonArray = getRestServiceProvider().get("/prescription_migration_vw?limit =" + limit)
//            }
            this.searchResults.clear()
            PrescriptionMigrationRecord[] prescriptionMigrationRecords = gson.fromJson(jsonArray.toString(), PrescriptionMigrationRecord[].class)
            if (prescriptionMigrationRecords != null && prescriptionMigrationRecords.length > 0) {
                this.searchResults.addAll(Arrays.asList(prescriptionMigrationRecords))
            }
            for (PrescriptionMigrationRecord record : this.searchResults) {
                record.setRestService(getRestServiceProvider())
            }
            return this.searchResults
        }
    }
}
