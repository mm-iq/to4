package States;

import java.util.Random;

import App.SimulationLogger;
import Models.*;

public class ActionState implements ITruckState {

    private final int actionTime;
    private final Random random = new Random();

    public ActionState() {
        
        if(random.nextDouble() < 0.05) {
            // fałszywy alarm → czas akcji 0 s
            this.actionTime = 0;
            SimulationLogger.addLog("Fałszywy alarm: ");
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
            SimulationLogger.addLog("🔙 Zakończyłem akcję 🚒 " + truck.getTruckId() + " ze stacji 🏢 " + truck.getTruckFireStation().getName());
            truck.setTruckState(new ReturningState());            
        }
        else {
            // kontynuacja akcji
            truck.incrementTime();
        }
    }

    @Override
    public int getTimeRemaining(Truck truck) {
        return Math.max(0, actionTime - truck.getElapsedTime());
    }

    @Override
    public String toString() {
        return "W AKCJI";
    }    
    
}
