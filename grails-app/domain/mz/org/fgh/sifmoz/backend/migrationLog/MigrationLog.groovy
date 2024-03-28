package mz.org.fgh.sifmoz.backend.migrationLog

import mz.org.fgh.sifmoz.backend.base.BaseEntity
import mz.org.fgh.sifmoz.backend.protection.Menu

import java.sql.Timestamp

class MigrationLog extends BaseEntity{

    String id;
    String errorCode;
    String[] errorDescription;
    int sourceId;
    String sourceEntity;
    String iDMEDId;
    String status
    String iDMEDEntity;
    Date creationDate = new Date()

    static constraints = {
        iDMEDId nullable: true
        iDMEDEntity nullable: true
        creationDate nullable: true
        //sourceId unique: [sourceEntity]
    }


    MigrationLog() {
    }

    MigrationLog(String errorCode, String[] errorDescription, int sourceId, String sourceEntity) {
        this.errorCode = errorCode
        this.errorDescription = errorDescription
        this.sourceId = sourceId
        this.sourceEntity = sourceEntity
        this.status = "REJECTED"
    }

    MigrationLog(String errorCode, String[] errorDescription, int sourceId, String sourceEntity, String iDMEDId, String iDMEDEntity, String status) {
        this.errorCode = errorCode
        this.errorDescription = errorDescription
        this.sourceId = sourceId
        this.sourceEntity = sourceEntity
        this.iDMEDId = iDMEDId
        this.iDMEDEntity = iDMEDEntity
        this.status = status
    }
    static mapping = {
        id generator: "uuid"
        sort: "source_entity"
    }

    @Override
    String getId() {
        return this.id
    }


    @Override
    public String toString() {
        return "MigrationLog{" +
                "errorCode='" + errorCode + '\'' +
                ", errorDescription=" + Arrays.toString(errorDescription) +
                ", sourceId=" + sourceId +
                ", sourceEntity='" + sourceEntity + '\'' +
                ", iDMEDId='" + iDMEDId + '\'' +
                ", iDMEDEntity='" + iDMEDEntity + '\'' +
                ", creationDate=" + creationDate +
                '}';
    }

    @Override
    List<Menu> hasMenus() {
        List<Menu> menus = new ArrayList<>()
        Menu.withTransaction {
            menus = Menu.findAllByCodeInList(Arrays.asList(migrationMenuCode))
        }
        return menus
    }
}
