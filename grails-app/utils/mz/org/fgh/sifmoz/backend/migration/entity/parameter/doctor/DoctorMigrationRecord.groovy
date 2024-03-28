package mz.org.fgh.sifmoz.backend.migration.entity.parameter.doctor

import com.fasterxml.jackson.annotation.JsonIgnore
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.clinicSectorType.ClinicSectorType
import mz.org.fgh.sifmoz.backend.dispenseType.DispenseType
import mz.org.fgh.sifmoz.backend.doctor.Doctor
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class DoctorMigrationRecord extends AbstractMigrationRecord {

     String emailaddress

     String firstname

     Integer id

     String lastname

     String mobileno

     char modified

     String telephoneno

     boolean active

     int category

    String migration_status

    @Override
    public List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        Doctor.withTransaction {
            getMigratedRecord().setFirstnames(this.firstname.trim())
            getMigratedRecord().setLastname(this.lastname.trim())
            getMigratedRecord().setGender("TBD")
            getMigratedRecord().setTelephone(this.telephoneno.trim())
            getMigratedRecord().setEmail(this.emailaddress.trim())
            getMigratedRecord().setClinic(Clinic.findByMainClinic(true))
            getMigratedRecord().setActive(true)

            if (Utilities.listHasElements(logs)) return logs


            getMigratedRecord().validate()
            if (!getMigratedRecord().hasErrors()) {
                getMigratedRecord().setId(UUID.randomUUID().toString())
                getMigratedRecord().save(flush: true)
            } else {
                logs.addAll(generateUnknowMigrationLog(this, getMigratedRecord().getErrors().toString()))
                return logs
            }
        }
        return logs
    }

    @Override
    public void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        return this.id
    }

    @Override
    String getEntityName() {
        return "doctor"
    }

    @Override
    Doctor initMigratedRecord() {
        return new Doctor();
    }

    @Override
    Doctor getMigratedRecord() {
        return (Doctor) super.getMigratedRecord();
    }
}
