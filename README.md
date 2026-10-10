Fleet Rental Management System (FRMS)

A Java console application developed for the Graduate Diploma in Software Engineering coursework. This system manages vehicle fleets, customer records, and rental agreements, featuring multithreading support for background status monitoring.

Key Features:
- Object-Oriented Design: Uses inheritance, polymorphism, abstract classes, and the Reportable interface.
- Multithreading: Background worker threads run as Daemon threads to monitor vehicle statuses and check overdue agreements every 5 seconds.
- Thread Synchronization: Safe resource access using synchronized methods.
- Java Streams & Lambdas: Used for filtering vehicles, sorting customers, and generating revenue reports.
- Custom Exceptions: Handles errors like VehicleNotAvailableException and InsufficientCreditException.
- User Authentication: Secure login and user registration system.

Technologies Used:
- Java (JDK 17 or higher)
- NetBeans IDE

How to Run:
1. Open the project in NetBeans IDE.
2. Ensure all source files are under the lk.ijse.fleet package.
3. Clean and build the project (Shift + F11).
4. Run the Main.java class (Shift + F6).
5. Register a user, log in, and use the terminal menu to manage the fleet.