package mz.org.fgh.sifmoz.backend.migration.entity.patient

import groovy.sql.Sql
import mz.org.fgh.sifmoz.backend.clinic.Clinic
import mz.org.fgh.sifmoz.backend.distribuicaoAdministrativa.Province
import mz.org.fgh.sifmoz.backend.healthInformationSystem.HealthInformationSystem
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migration.common.MigrationError
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.patient.Patient
import mz.org.fgh.sifmoz.backend.utilities.Utilities
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

public class PatientMigrationRecord extends AbstractMigrationRecord {

    def patientService
    DataSource dataSource

    int id;
    boolean accountstatus;
    boolean isPatientEmTransito = false
    String address1;
    String address2;
    String address3;
    String cellphone;
    Date dateofbirth;
    int clinicid;
    String clinicname;
    String clinicuuid;
    String nextOfKinName;
    String nextOfKinPhone;
    String firstnames;
    String homePhone;
    String lastname;
    char modified;
    String patientid;
    String province;
    char sex;
    String workPhone;
    String race;
    String uuidopenmrs;
    String uuidlocationopenmrs;

    @Override
    List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()

        // Get current SessionFactory
        Patient.withNewTransaction {
            Clinic clinic = Clinic.findByMainClinic(true)

            if (getMigratedRecord().getMatchId() == null) {
                def  patient =  Patient.findAll( [max: 2, sort: ['matchId': 'desc']])
//                def  patient =  Patient.findAll( [sort: ['matchId': 'desc']])
                if (patient.size() == 0) {
                    getMigratedRecord().setMatchId(1)
                } else {
                    getMigratedRecord().setMatchId(patient.get(0).matchId + 1)
                }
            }

            getMigratedRecord().setAddress(this.getAddress3() + " " + getAddress1())
            getMigratedRecord().setAddressReference(this.getAddress2())
            getMigratedRecord().setAccountstatus(this.accountstatus)

            if (!Utilities.stringHasValue(this.firstnames)) throw new RuntimeException("O paciente nao possui nome")

            String[] names = this.firstnames.split(" ")
            String firstName = names[0]
            String midleName = null
            if (names.length > 1) {
                for (int h = 1; h < names.length; h++) {
                    if (!Utilities.stringHasValue(midleName)) midleName = names[h]
                    else midleName += " " + names[h]
                }
            }

            getMigratedRecord().setFirstNames(firstName)
            getMigratedRecord().setId(this.getUuidopenmrs())
            getMigratedRecord().setCellphone(this.getCellphone())
            getMigratedRecord().setDateOfBirth(this.dateofbirth)
            getMigratedRecord().setHisUuid(this.uuidopenmrs)
            getMigratedRecord().setHisLocation(this.uuidlocationopenmrs)
            setClinicToMigratedRecord(logs)
            setProvinceToMigratedRecord(logs, this.province)
            getMigratedRecord().setMiddleNames(midleName)
            getMigratedRecord().setLastNames(this.getLastname())
            getMigratedRecord().setGender(this.getSex() == 'M' ? "Masculino" : "Feminino")
            getMigratedRecord().setDistrict(getMigratedRecord().getClinic().getDistrict())
            getMigratedRecord().setHis(HealthInformationSystem.findByAbbreviation('OpenMRS'))
            getMigratedRecord().setHisSyncStatus('N' as char)
            getMigratedRecord().setClinic(clinic)

            if (Utilities.listHasElements(logs)) return logs
            if (getMigratedRecord().id == null || getMigratedRecord().id.length() <= 0) getMigratedRecord().setId(UUID.randomUUID().toString())
            getMigratedRecord().beforeInsert()
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
    void updateIDMEDInfo() {

    }

    @Override
    int getId() {
        return this.id
    }

    @Override
    String getEntityName() {
        return "Patient"
    }

    @Override
    MigratedRecord initMigratedRecord() {
        return new Patient()
    }

    @Override
    Patient getMigratedRecord() {
        return (Patient) super.getMigratedRecord()
    }

    void setClinicToMigratedRecord(List<MigrationLog> logs) {
        Clinic clinic = null
        Clinic.withTransaction {
            clinic = Clinic.findByMainClinic(true)
        }
        if (clinic != null) {
            getMigratedRecord().setClinic(clinic)
        } else {
            String[] msg = { String.format(MigrationError.CLINIC_NOT_FOUND.getDescription(), this.clinicuuid) }
            logs.add(new MigrationLog(MigrationError.CLINIC_NOT_FOUND.getCode(), msg, getId(), getEntityName()))
        }
    }

    void setProvinceToMigratedRecord(List<MigrationLog> logs, String provinceName) {
        Province province = null
        Province.withTransaction {
            province = Province.findByDescription(provinceName)
        }
        if (province != null) {
            getMigratedRecord().setProvince(province)
        } else {
            getMigratedRecord().setProvince(getMigratedRecord().getClinic().getProvince())
        }
    }

}
