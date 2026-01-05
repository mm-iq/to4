package States;

import java.util.Random;

import Models.*;

public class EnRouteState implements ITruckState {

    private final int travelTime;
    private final Random random = new Random();

    public EnRouteState() {
        // losujemy czas dojazdu 0-3 sek
        this.travelTime = random.nextInt(4);
    }

    @Override
    public void update(Truck truck) {

        // jeśli skończył się czas dojazdu
        if(truck.getElapsedTime() >= travelTime) {
            // zmieniamy stan danego wozu z dojazdu → akcja
            truck.setTruckState(new ActionState());
        }

        // zwiększamy licznik czasu w wozie
        truck.incrementTime();
    }

    @Override
    public int getTimeRemaining(Truck truck) {
        // Czas całkowity minus to co już upłynęło
        return Math.max(0, travelTime - truck.getElapsedTime());
    }

    @Override
    public String toString() {
        return "DOJAZD";
    }
    
}