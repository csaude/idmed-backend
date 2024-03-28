package mz.org.fgh.sifmoz.backend.migration.entity.dispense;

import mz.org.fgh.sifmoz.backend.migrationLog.MigrationLog;
import mz.org.fgh.sifmoz.backend.migration.base.record.AbstractMigrationRecord;
import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord;

import java.util.Date;
import java.util.List;

public class PackageDrugInfoTmpMigrationRecord extends AbstractMigrationRecord {

    private Integer id;
    private String drugname;
    private Integer qtyinhand;
    private Integer packageid;
    private String dateexpectedstring;
    private Date dispensedate;

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDrugname() {
        return drugname;
    }

    public void setDrugname(String drugname) {
        this.drugname = drugname;
    }

    public Integer getQtyinhand() {
        return qtyinhand;
    }

    public void setQtyinhand(Integer qtyinhand) {
        this.qtyinhand = qtyinhand;
    }

    public Integer getPackageid() {
        return packageid;
    }

    public void setPackageid(Integer packageid) {
        this.packageid = packageid;
    }

    public String getDateexpectedstring() {
        return dateexpectedstring;
    }

    public void setDateexpectedstring(String dateexpectedstring) {
        this.dateexpectedstring = dateexpectedstring;
    }

    public Date getDispensedate() {
        return dispensedate;
    }

    public void setDispensedate(Date dispensedate) {
        this.dispensedate = dispensedate;
    }

    @Override
    public List<MigrationLog> migrate() {
        return null;
    }

    @Override
    public void updateIDMEDInfo() {

    }

    @Override
    public int getId() {
        return 0;
    }

    @Override
    public String getEntityName() {
        return null;
    }

    @Override
    public MigratedRecord initMigratedRecord() {
        return null;
    }
}
