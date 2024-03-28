package mz.org.fgh.sifmoz.backend.migration.entity.parameter.clinic

import mz.org.fgh.sifmoz.backend.attributeType.PatientAttributeType
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.District
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.facilityType.FacilityType
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class ClinicMigrationRecord extends AbstractMigrationRecord {

    Integer id

    boolean mainclinic

    String notes

    String code

    String telephone

    String clinicname

    String province

    String district

    String subdistrict

    String uuid

    String facilitytype

    String migration_status

    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        Clinic.withTransaction {

            if (this.mainclinic) {
                Clinic clinic = Clinic.findByMainClinic(true)
                clinic.uuid = this.uuid
                clinic.save(flush: true)
            } else {
                getMigratedRecord().setId(this.uuid)
                getMigratedRecord().setNotes(this.notes)
                getMigratedRecord().setTelephone(this.telephone)
                getMigratedRecord().setMainClinic(this.mainclinic)
                getMigratedRecord().setClinicName(this.clinicname)
                getMigratedRecord().setProvince(Province.findByDescription(this.province.length() == 0 ? Clinic.findByMainClinic(true).province.description : this.province))
                getMigratedRecord().setDistrict(District.findByDescriptionIlike("%" + this.district + "%"))
                getMigratedRecord().setFacilityType(FacilityType.findByDescriptionIlike("%" + this.facilitytype + "%"))
                getMigratedRecord().setMainClinic(false)
                getMigratedRecord().setCode(this.code)
                getMigratedRecord().setActive(true)
                getMigratedRecord().setUuid(this.uuid)

                if (Utilities.listHasElements(logs)) return logs

                getMigratedRecord().validate()
                if (!getMigratedRecord().hasErrors()) {
                    getMigratedRecord().save(flush: true)
                } else {
                    logs.addAll(generateUnknowMigrationLog(this, getMigratedRecord().getErrors().toString()))
                    return logs
                }
            }
        }

        return logs;
    }

    @Override
    void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        return this.id
    }

    @Override
    String getEntityName() {
        return "clinic"
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new Clinic()
    }

    @Override
    Clinic getMigratedRecord() {
        return (Clinic) super.getMigratedRecord()
    }
}
