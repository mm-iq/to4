package Models;

import java.util.ArrayList;
import java.util.List;

import App.SimulationLogger;
import Observers.*;
import States.*;

public class FireStation implements IObserver {
    
    private List<Truck> trucks = new ArrayList<>();
    private double latitude;
    private double longitude;
    private String name;

    public FireStation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void addTruck(Truck truck) {
        this.trucks.add(truck);
    }

    public double calculateDistance(double incidentLatitude, double incidentLongitude) {
        return Math.sqrt(Math.pow(this.latitude - incidentLatitude, 2) + Math.pow(this.longitude - incidentLongitude, 2));
    }
    
    @Override
    public void update(Incident incident) {
        
        SimulationLogger.addLog("STACJA [" + this.name + "]: Otrzymałem zgłoszenie o " +
        incident.getType() + " na pozycji (" + incident.getLatitude() + ", " + 
        incident.getLongitude() + ")");
        /*
        if(liczbaWolnychWozow > liczbaPotrzebnych)
        wysyłam
        else
            iterator.next.take
        */
    }
    
    // gettery i settery

    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    
    public List<Truck> getAvailableTrucks() {
        List<Truck> availableTrucks = new ArrayList<>();
        for(Truck truck : trucks) {
            // wóz jest wolny, gdy jego stan → IdleState
            if(truck.getTruckState() instanceof IdleState) {
                availableTrucks.add(truck);
            }
        }
        return availableTrucks;
    }
    
    public List<Truck> getAllTrucks() {
        return this.trucks;
    }
    
    public String getName() { return this.name; }

}
