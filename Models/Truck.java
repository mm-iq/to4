package Models;

import States.*;

public class Truck {

    private ITruckState currentTruckState;
    private int elapsedTime;
    private FireStation truckFireStation;
    private final int truckId;
    private static int idCounter;

    public Truck(FireStation fireStation) {
        this.currentTruckState = new IdleState();
        this.elapsedTime = 0;
        this.truckFireStation = fireStation;
        this.truckId = idCounter++;
    }

    
    public void incrementTime() {
        this.elapsedTime++;
    }
    
    public void updateTruckState() {
        currentTruckState.update(this);
    }
    
    // gettery i settery
    public ITruckState getTruckState() {
        return this.currentTruckState;
    }
    public void setTruckState(ITruckState state) {
        this.currentTruckState = state;
        this.elapsedTime = 0;
    }

    public int getElapsedTime() {
        return this.elapsedTime;
    }

    public int getTruckId() {
        return this.truckId;
    }

    public FireStation getTruckFireStation() {
        return this.truckFireStation;
    }
}
