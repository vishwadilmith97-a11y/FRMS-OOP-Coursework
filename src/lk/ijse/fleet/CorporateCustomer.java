package lk.ijse.fleet;

public class CorporateCustomer extends Customer {
    private double creditLimit;
    private double outstandingBalance;

    public CorporateCustomer(String fullName, String contactNumber, double creditLimit) {
        super(fullName, contactNumber);
        this.creditLimit = creditLimit;
        this.outstandingBalance = 0;
    }

    public double getCreditLimit() { return creditLimit; }
    public void setCreditLimit(double creditLimit) { this.creditLimit = creditLimit; }

    public double getOutstandingBalance() { return outstandingBalance; }
    public void setOutstandingBalance(double outstandingBalance) { this.outstandingBalance = outstandingBalance; }

    @Override
    public double applyDiscount(double subtotal) {
        return subtotal * 0.85; // flat 15% corporate discount
    }

    public boolean hasSufficientCredit(double cost) {
        return (outstandingBalance + cost) <= creditLimit;
    }

    @Override
    public String toString() {
        return String.format("%s | Corporate | Credit limit: %,.2f | Outstanding: %,.2f",
                super.toString(), creditLimit, outstandingBalance);
    }
}
