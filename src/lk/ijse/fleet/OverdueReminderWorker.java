package lk.ijse.fleet;

public class OverdueReminderWorker implements Runnable {
    private final FleetManager fleetManager;

    public OverdueReminderWorker(FleetManager fleetManager) {
        this.fleetManager = fleetManager;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Thread.sleep(5000);
                fleetManager.checkOverdueAgreements();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
