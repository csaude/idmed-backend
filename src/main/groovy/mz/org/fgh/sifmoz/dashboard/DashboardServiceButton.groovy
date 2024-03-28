package mz.org.fgh.sifmoz.dashboard

class DashboardServiceButton extends AbstractValidateble{
    String id = UUID.randomUUID()
    int quantity
    String service

    DashboardServiceButton(int quantity, String service) {
        this.id = UUID.randomUUID()
        this.quantity = quantity
        this.service = service
    }
}
