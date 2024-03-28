package mz.org.fgh.sifmoz.backend.migration.base.log;

public class AbstractMigrationLog {
    private String id;
    private String errorCode;
    private String description;

    private String originEntityId;

    private String entityName;

    public AbstractMigrationLog(String id, String errorCode, String description) {
        this.id = id;
        this.errorCode = errorCode;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOriginEntityId() {
        return originEntityId;
    }

    public void setOriginEntityId(String originEntityId) {
        this.originEntityId = originEntityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
