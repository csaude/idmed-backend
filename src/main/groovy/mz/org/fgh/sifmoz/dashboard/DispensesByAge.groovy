package mz.org.fgh.sifmoz.dashboard

class DispensesByAge extends AbstractValidateble{
    String id = UUID.randomUUID()
    int adulto
    int menor
    String dispenseType

    DispensesByAge(int adulto, int menor, String dispenseType) {
        this.id = UUID.randomUUID()
        this.adulto = adulto
        this.menor = menor
        this.dispenseType = dispenseType
    }
}
