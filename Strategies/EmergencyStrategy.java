package Strategies;

public class EmergencyStrategy implements IIncidentStrategy {

    @Override
    public int getRequiredTruckCount(){
        
        return 2;
    }
}