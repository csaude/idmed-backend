package mz.org.fgh.sifmoz.dashboard

class PatientsFirstDispenseByAge extends AbstractValidateble{
    String id = UUID.randomUUID()
    int quantity
    int month
    String faixa

    PatientsFirstDispenseByAge(int quantity, int month, String faixa) {
        this.id = UUID.randomUUID()
        this.quantity = quantity
        this.month = month
        this.faixa = faixa
    }
}
