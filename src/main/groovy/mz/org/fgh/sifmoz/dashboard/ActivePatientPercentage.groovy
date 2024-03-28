package mz.org.fgh.sifmoz.dashboard

import java.math.RoundingMode
import java.text.DecimalFormat

class ActivePatientPercentage extends AbstractValidateble{
    String id = UUID.randomUUID()
    int quantity
    double percentage
    String gender

    ActivePatientPercentage(int quantity, double percentage, String gender) {
        this.id = UUID.randomUUID()
        this.quantity = quantity
        DecimalFormat df = new DecimalFormat("0.00")
        df.setRoundingMode(RoundingMode.UP);
        this.percentage = Double.parseDouble(df.format(percentage))
        this.gender = gender
    }
}
