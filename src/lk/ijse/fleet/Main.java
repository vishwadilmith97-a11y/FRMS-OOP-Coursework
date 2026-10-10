package lk.ijse.fleet;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final String LINE = "========================================================";

    private final FleetManager fleetManager;
    private final UserManager userManager;
    private final Scanner scanner;
    private SystemUser loggedInUser; // null until login succeeds

    public Main() {
        this.fleetManager = new FleetManager();
        this.userManager = new UserManager();
        this.scanner = new Scanner(System.in);

        // Lambda implementing the custom functional interface
        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId())
        );

        // Background workers as daemon threads
        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();
    }

    public static void main(String[] args) {
        new Main().run();
    }

    public void run() {
        while (true) {
            showAuthMenu();      // returns only after a successful login (Exit terminates)
            showMainMenu();      // returns on logout
        }
    }

    // ------------------------------------------------------------------ auth

    public void showAuthMenu() {
        while (loggedInUser == null) {
            System.out.println();
            System.out.println(LINE);
            System.out.println("    FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println(LINE);
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            System.out.print("Enter choice > ");
            String choice = readLine().trim();
            switch (choice) {
                case "1" -> {
                    String username = readNonEmpty("Username: ");
                    String password = readNonEmpty("Password: ");
                    SystemUser user = userManager.login(username, password);
                    if (user != null) {
                        loggedInUser = user;
                    } else {
                        System.out.println("Invalid username or password");
                    }
                }
                case "2" -> {
                    String username = readNonEmpty("Choose a username: ");
                    String password = readNonEmpty("Choose a password: ");
                    String fullName = readNonEmpty("Full name: ");
                    userManager.registerUser(username, password, fullName);
                }
                case "0" -> {
                    System.out.println("Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice. Please enter 1, 2 or 0.");
            }
        }
    }

    // ------------------------------------------------------------- main menu

    public void showMainMenu() {
        System.out.println();
        System.out.println("Welcome, " + loggedInUser.getFullName() + "! Loading fleet dashboard...");
        while (loggedInUser != null) {
            System.out.println();
            System.out.println(LINE);
            System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
            System.out.println(LINE);
            System.out.println("[1] Add Vehicle");
            System.out.println("[2] Add Customer");
            System.out.println("[3] View Available Vehicles");
            System.out.println("[4] Create Rental Agreement");
            System.out.println("[5] Return Vehicle");
            System.out.println("[6] View Revenue Report");
            System.out.println("[7] View Vehicles by Fuel Type");
            System.out.println("[8] View Combined Fleet & Customer Report");
            System.out.println("[9] View All Customers");
            System.out.println("[10] View All Rental Agreements");
            System.out.println("[0] Logout");
            System.out.print("Enter choice > ");
            try {
                int choice = Integer.parseInt(readLine().trim());
                handleMainChoice(choice);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number from the menu.");
            }
        }
    }

    private void handleMainChoice(int choice) {
        switch (choice) {
            case 1 -> addVehicle();
            case 2 -> addCustomer();
            case 3 -> printList(fleetManager.getAvailableVehicles(), "No available vehicles.");
            case 4 -> createRental();
            case 5 -> fleetManager.returnVehicle(readNonEmpty("Agreement ID (e.g. AGR-1001): "));
            case 6 -> {
                System.out.println("================================================================");
                System.out.println("FLEET REVENUE REPORT");
                System.out.println("================================================================");
                System.out.printf("Total Revenue: %,.2f%n", fleetManager.generateRevenueReport());
                System.out.println("----------------------------------------------------------------");
            }
            case 7 -> printList(fleetManager.getVehiclesByFuelType(readFuelType()),
                    "No vehicles with that fuel type.");
            case 8 -> fleetManager.printAllReportables();
            case 9 -> printList(fleetManager.getAllCustomers(), "No customers registered.");
            case 10 -> printList(fleetManager.getAllAgreements(), "No rental agreements yet.");
            case 0 -> {
                System.out.println("Logged out. See you, " + loggedInUser.getFullName() + "!");
                loggedInUser = null;
            }
            default -> System.out.println("Invalid choice. Please pick a number from the menu.");
        }
    }

    // ------------------------------------------------------------ operations

    private void addVehicle() {
        System.out.println("Vehicle type: [1] Car  [2] Van  [3] Truck");
        int type = readInt("Choose type > ", 1, 3);
        String reg = readNonEmpty("Registration number: ");
        String brand = readNonEmpty("Brand: ");
        String model = readNonEmpty("Model: ");
        double rate = readDouble("Daily rate: ");
        FuelType fuel = readFuelType();

        Vehicle vehicle;
        switch (type) {
            case 1 -> vehicle = new Car(reg, brand, model, rate, fuel);
            case 2 -> vehicle = new Van(reg, brand, model, rate, fuel,
                    readInt("Passenger capacity: ", 1, 100));
            default -> vehicle = new Truck(reg, brand, model, rate, fuel,
                    readDouble("Cargo capacity (tons): "));
        }
        fleetManager.addVehicle(vehicle);
        System.out.println("Vehicle added: " + vehicle.getVehicleId());
    }

    private void addCustomer() {
        System.out.println("Customer type: [1] Individual  [2] Corporate");
        int type = readInt("Choose type > ", 1, 2);
        String name = readNonEmpty("Full / company name: ");
        String contact = readNonEmpty("Contact number: ");
        Customer customer;
        if (type == 1) {
            customer = new IndividualCustomer(name, contact, readInt("Loyalty points: ", 0, Integer.MAX_VALUE));
        } else {
            customer = new CorporateCustomer(name, contact, readDouble("Credit limit: "));
        }
        fleetManager.addCustomer(customer);
        System.out.println("Customer added: " + customer.getCustomerId());
    }

    private void createRental() {
        String vehicleId = readNonEmpty("Vehicle ID (e.g. VEH-1001): ");
        String customerId = readNonEmpty("Customer ID (e.g. CUS-1001): ");
        int days = readInt("Rental days: ", 1, 3650);
        try {
            RentalAgreement agreement = fleetManager.createRentalAgreement(vehicleId, customerId, days);
            if (agreement != null) {
                System.out.println(agreement);
            }
        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
    }

    private <T> void printList(List<T> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (T item : items) {
            System.out.println(item);
        }
    }

    // ----------------------------------------------------------- input helpers

    private String readLine() {
        try {
            return scanner.nextLine();
        } catch (NoSuchElementException e) { // input stream closed
            System.out.println("\nInput closed. Exiting.");
            System.exit(0);
            return "";
        }
    }

    private String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = readLine().trim();
            if (!s.isEmpty()) {
                return s;
            }
            System.out.println("Input cannot be empty.");
        }
    }

    private int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int v = Integer.parseInt(readLine().trim());
                if (v >= min && v <= max) {
                    return v;
                }
                System.out.println("Please enter a number between " + min + " and "
                        + (max == Integer.MAX_VALUE ? "a large value" : String.valueOf(max)) + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double v = Double.parseDouble(readLine().trim());
                if (v > 0 && !Double.isInfinite(v)) {
                    return v;
                }
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Try again.");
            }
        }
    }

    private FuelType readFuelType() {
        FuelType[] types = FuelType.values();
        StringBuilder sb = new StringBuilder("Fuel type: ");
        for (int i = 0; i < types.length; i++) {
            sb.append("[").append(i + 1).append("] ").append(types[i]).append("  ");
        }
        System.out.println(sb.toString().trim());
        return types[readInt("Choose fuel type > ", 1, types.length) - 1];
    }
}
