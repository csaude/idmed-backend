package mz.org.fgh.sifmoz.backend.migration.entity.parameter.clinicSector

import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.clinicSector.ClinicSector
import mz.org.fgh.sifmoz.backend.clinicSectorType.ClinicSectorType
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migration.entity.parameter.clinic.ClinicMigrationRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class ClinicSectorMigrationRecord extends AbstractMigrationRecord {

     Integer id

     String code

     String sectorname

     String telephone

     String uuid

     String clinicsectortype

     String clinicuuid

    String migration_status

    @Override
     List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        ClinicSector.withTransaction {
            getMigratedRecord().setId(this.uuid)
            getMigratedRecord().setCode(this.code)
            getMigratedRecord().setDescription(this.sectorname)
            getMigratedRecord().setUuid(this.uuid)
            getMigratedRecord().setClinic(Clinic.findByMainClinic(true))
            getMigratedRecord().setClinicSectorType(ClinicSectorType.findByCode(this.clinicsectortype))
            getMigratedRecord().setSyncStatus('S')
            getMigratedRecord().setActive(true)

            if (Utilities.listHasElements(logs)) return logs


            getMigratedRecord().validate()
            if (!getMigratedRecord().hasErrors()) {
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
        return "clinicSector"
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new ClinicSector();
    }

    @Override
    ClinicSector getMigratedRecord() {
        return (ClinicSector) super.getMigratedRecord();
    }
}
