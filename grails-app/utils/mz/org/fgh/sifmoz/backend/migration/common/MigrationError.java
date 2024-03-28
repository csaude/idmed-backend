package mz.org.fgh.sifmoz.backend.migration.common;

public enum MigrationError {
    CLINIC_NOT_FOUND("E001", "A clinica com uuid: {0} no IDART, nao existe no IDMED"),
    PROVINCE_NOT_FOUND("E002", "A Provincia com description: {0} do IDART, nao existe no IDMED"),
    PATIENT_NOT_FOUND("E003", "O paciente com patient_id: {0} do IDART, nao existe no IDMED"),

    IDENTIFIER_TYPE_NOT_FOUND("E004", "O tipo de Identificador com o code: {0} do IDART, nao existe no IDMED"),

    //STOCK
    STOCK_CENTER_NOT_FOUND("E005", "O stock center com o nome: {0} do IDART, nao existe no IDMED"),
    DRUG_NOT_FOUND("E006", "O medicamento: {0} do IDART, nao existe no IDMED"),
    STOCK_ENTRANCE_NOT_FOUND("E007", "A entrada de stock do IDART, nao existe no IDMED"),
    MAIN_CLINIC_NOT_IDENTIFIED("E008", "A clinica principal nao foi identificada no IDMED"),

    STOCK_NOT_FOUND("E010", "O stock do IDART, nao existe no IDMED"),
    INVENTORY_NOT_FOUND("E011", "O inventario do IDART, nao existe no IDMED");

    private final String code;
    private final String description;

    private MigrationError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
