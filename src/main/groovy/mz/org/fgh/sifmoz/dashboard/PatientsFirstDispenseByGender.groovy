package mz.org.fgh.sifmoz.dashboard

class PatientsFirstDispenseByGender extends AbstractValidateble{
    String id = UUID.randomUUID()
    int quantity
    int month
    String gender

    PatientsFirstDispenseByGender(int quantity, int month, String gender) {
        this.id = UUID.randomUUID()
        this.quantity = quantity
        this.month = month
        this.gender = gender
    }
}
