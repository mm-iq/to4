package States;

import java.util.Random;

import Models.*;

public class ReturningState implements ITruckState {

    private final int travelTime;
    private final Random random = new Random();    

    public ReturningState() {
        // losowanie czasu powrotu 0-3 sek
        this.travelTime = random.nextInt(4);
    }

    @Override
    public void update(Truck truck) {

        // jeśli skończył się czas powrotu
        if(truck.getElapsedTime() >= travelTime) {
            // zmieniamy stan danego wozu z powrotu → bezczynny
            truck.setTruckState(new IdleState());
        }

        // zwiększamy licznik czasu w wozie
        truck.incrementTime();
    }

    @Override
    public int getTimeRemaining(Truck truck) {
        return Math.max(0, travelTime - truck.getElapsedTime());
    }

    @Override
    public String toString() {
        return "POWRÓT";
    }
}