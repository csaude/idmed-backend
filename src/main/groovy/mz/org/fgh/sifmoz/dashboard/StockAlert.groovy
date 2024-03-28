package mz.org.fgh.sifmoz.dashboard

class StockAlert  extends AbstractValidateble{
    String id
    String drug
    int balance
    double avgConsuption
    String state

    StockAlert(String id, String drug, int balance, double avgConsuption, String state) {
        this.id = id
        this.drug = drug
        this.balance = balance
        this.avgConsuption = avgConsuption
        this.state = state
    }
}
