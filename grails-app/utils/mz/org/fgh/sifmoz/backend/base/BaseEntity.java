package mz.org.fgh.sifmoz.backend.base;

import mz.org.fgh.sifmoz.backend.migration.base.record.MigratedRecord;
import mz.org.fgh.sifmoz.backend.utilities.IRoleMenu;

public abstract class BaseEntity implements MigratedRecord, IRoleMenu {

    public boolean isValidated() {
        return false;
    }

    @Override
    public String getEntity() {
        return this.getClass().getName();
    }


    public static final String patientMenuCode = "01";
    public static final String  groupsMenuCode = "02";
    public static final String  stockMenuCode = "03";
    public static final String  dashboardMenuCode = "04";
    public static final String  reportsMenuCode = "05";
    public static final String  administrationMenuCode = "06";
    public static final String  migrationMenuCode = "07";

    public static final String  homeMenuCode = "08";
}
