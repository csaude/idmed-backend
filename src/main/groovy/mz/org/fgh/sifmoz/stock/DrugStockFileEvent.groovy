package mz.org.fgh.sifmoz.stock

import mz.org.fgh.sifmoz.dashboard.AbstractValidateble

class DrugStockFileEvent extends AbstractValidateble{
    String id = UUID.randomUUID()
    String year
    String month
    Date eventDate
    String moviment
   // String orderNumber
    long incomes
    long outcomes
    long posetiveAdjustment
    long negativeAdjustment
    long loses
    long balance
    //String notes
    String code
    String stockId

    void calculateBalance(long previousBalance) {
        this.balance =(previousBalance + incomes  + posetiveAdjustment) - ( outcomes + negativeAdjustment + loses )
    }

}
