package mz.org.fgh.sifmoz.backend.migration.stage

import mz.org.fgh.sifmoz.backend.base.BaseEntity

class MigrationStage {

    public static final String STAGE_COMPLETED = "COMPLETED"
    public static final String STAGE_IN_PROGRESS = "IN_PROGRESS"
    public static final String STAGE_NOT_STARTED = "NOT_STARTED"

    String id
    String code
    String value

    static mapping = {
        id generator: "assigned"
        id column: 'id', index: 'Pk_MigrationStage_Idx'
    }

    def beforeInsert() {
        if (!id) {
            id = UUID.randomUUID()
        }
    }

    static constraints = {
        code unique: true
    }

}
