package mz.org.fgh.sifmoz.dashboard

class DispensesByGender extends AbstractValidateble{
    String id = UUID.randomUUID()
    int masculino
    int femenino
    String dispenseType

    DispensesByGender(int masculino, int femenino, String dispenseType) {
        this.id = UUID.randomUUID()
        this.masculino = masculino
        this.femenino = femenino
        this.dispenseType = dispenseType
    }
}
