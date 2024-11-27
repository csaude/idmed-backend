package mz.org.fgh.sifmoz.backend.migration.entity.parameter.regimeTerapeutico

import com.fasterxml.jackson.annotation.JsonBackReference
import mz.org.fgh.sifmoz.backend.drug.Drug
import mz.org.fgh.sifmoz.backend.duration.Duration
import mz.org.fgh.sifmoz.backend.form.Form
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord
import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog
import mz.org.fgh.sifmoz.backend.service.ClinicalService
import mz.org.fgh.sifmoz.backend.therapeuticRegimen.TherapeuticRegimen
import mz.org.fgh.sifmoz.backend.utilities.Utilities

class RegimeTerapeuticoMigrationRecord extends AbstractMigrationRecord {

     Integer regimeid

     String regimeesquema

     boolean active

     String regimenomeespecificado

     String codigoregime

     String tipodoenca

     String regimeesquemaidart

    String migration_status


    @Override
     List<MigrationLog> migrate() {
        List<MigrationLog> logs = new ArrayList<>()
        TherapeuticRegimen regimenAux = null
        TherapeuticRegimen.withTransaction {
            regimenAux = TherapeuticRegimen.findByCode(this.codigoregime)

        if (regimenAux != null) {
            setMigratedRecord(regimenAux)
            return null
        }

        getMigratedRecord().setId(!regimenAux ? UUID.randomUUID().toString() : regimenAux.id)
        getMigratedRecord().setRegimenScheme(this.regimeesquema)
        getMigratedRecord().setCode(this.codigoregime)
        getMigratedRecord().setDescription(this.regimeesquema)
        getMigratedRecord().setOpenmrsUuid(this.regimenomeespecificado)
       //  getMigratedRecord().setClinicalService(ClinicalService.findByCode(this.tipodoenca))
        getMigratedRecord().setActive(false)

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
        return this.regimeid
    }

    @Override
    String getEntityName() {
        return "regimeterapeutico"
    }

    @Override
    TherapeuticRegimen initMigratedRecord() {
        return new TherapeuticRegimen();
    }

    @Override
    TherapeuticRegimen getMigratedRecord() {
        return (TherapeuticRegimen) super.getMigratedRecord();
    }

    @Override
    String getIdFieldName() {
        return "regimeid"
    }
}
