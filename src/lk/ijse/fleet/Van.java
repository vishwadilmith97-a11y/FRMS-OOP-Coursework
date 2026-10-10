package lk.ijse.fleet;

public class Van extends Vehicle {
    private int passengerCapacity;

    public Van(String registrationNumber, String brand, String model,
               double dailyRate, FuelType fuelType, int passengerCapacity) {
        super(registrationNumber, brand, model, dailyRate, fuelType);
        this.passengerCapacity = passengerCapacity;
    }

    public int getPassengerCapacity() { return passengerCapacity; }
    public void setPassengerCapacity(int passengerCapacity) { this.passengerCapacity = passengerCapacity; }

    @Override
    public double calculateRentalCost(int rentalDays) {
        // one-time flat 20% surcharge (NOT per day)
        return (getDailyRate() * rentalDays * getFuelType().getCostMultiplier())
                + (getDailyRate() * 0.20);
    }

    @Override
    public String toString() {
        return super.toString() + " | Seats: " + passengerCapacity;
    }
}
