package Strategies;

public class FireStrategy implements IIncidentStrategy {

    @Override
    public int getRequiredTruckCount(){
        
        return 3;
    }
}