package States;

import java.util.Random;

import Models.*;

public class ActionState implements ITruckState {

    private final int actionTime;
    private final Random random = new Random();

    public ActionState() {
        
        if(random.nextDouble() < 0.05) {
            // fałszywy alarm → czas akcji 0 s
            this.actionTime = 0;
            System.out.print("Fałszywy alarm: ");
        }
        else {
            // w przeciwnym razie → losowanie czasu 5-25 s
            this.actionTime = random.nextInt(21) + 5;
        }
    }

    @Override
    public void update(Truck truck) {

        // jeśli zakończył się czas akcji
        if(truck.getElapsedTime() >= actionTime) {
            // zakończenie akcji
            System.out.print("Zakończyłem akcję: id" + truck.getTruckId() + "ze stacji: " + truck.getTruckFireStation());
            truck.setTruckState(new ReturningState());            
        }
        else {
            // kontynuacja akcji
            truck.incrementTime();
        }
    }

    @Override
    public String toString() {
        return "W AKCJI";
    }    
    
}
