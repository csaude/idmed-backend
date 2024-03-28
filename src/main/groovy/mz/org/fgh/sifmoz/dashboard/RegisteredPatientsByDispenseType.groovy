package mz.org.fgh.sifmoz.dashboard

class RegisteredPatientsByDispenseType extends AbstractValidateble{
    String id = UUID.randomUUID()
    String dispense_type
    int quantity
    int month

    RegisteredPatientsByDispenseType(String dispense_type, int quantity, int month) {
        this.id = UUID.randomUUID()
        this.dispense_type = dispense_type
        this.quantity = quantity
        this.month = month
    }
}
