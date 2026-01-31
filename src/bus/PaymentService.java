package bus;

public class PaymentService {
    public double calculateChange(double totalAmount, double customerGiven) {
        return customerGiven - totalAmount;
    }

    public boolean isValidPayment(double totalAmount, double customerGiven) {
        return customerGiven >= totalAmount;
    }
}