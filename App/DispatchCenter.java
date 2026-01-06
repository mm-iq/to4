package App;
import java.util.ArrayList;
import java.util.List;

import Models.*;
import Models.Incident.IncidentType;
import Observers.*;
import States.*;
import Strategies.*;

public class DispatchCenter {

    // strategia zależna od rodzaju zdarzenia
    private IIncidentStrategy incidentStrategy;

    // lista wszystkich jednostek
    private List<IObserver> observers = new ArrayList<>();

    // zarządzanie obserwatorem
    public void addObserver(IObserver observer){
        observers.add(observer);
    }

    public void removeObserver(IObserver observer) {
        observers.remove(observer);
    }

    public void notifyObservers(Incident incident) {
        for(IObserver observer : observers) {
            observer.update(incident);
        }
    }

    // funkcja do ustalenia strategii dla danego zdarzenia
    public void handleIncident(Incident incident, List<FireStation> fireStations) {
        
        IncidentType incidentType = incident.getType();
        switch(incidentType) {
                
            case POZAR:
                this.setIncidentStrategy(new FireStrategy());
                break;
            case MIEJSCOWE_ZDARZENIE:
                this.setIncidentStrategy(new EmergencyStrategy());
                break;
            default:
                SimulationLogger.addLog("Błąd: Nieobsługiwalny typ zdarzenia!");
        }

        this.notifyObservers(incident);
        this.dispatchTrucks(incident, fireStations);
    }

    // funkcja do wysłania wozów
    public void dispatchTrucks(Incident incident, List<FireStation> allFireStations) {

        if(incidentStrategy == null){
            SimulationLogger.addLog("Błąd: Nie ustawiono strategii dysponowania!");
            return ;
        }        

        int requiredTrucks = incidentStrategy.getRequiredTruckCount(); // potrzebne wozy
        int dispatchedTrucks = 0; // wysłane wozy

        // iterator po najbliższych stacjach od miejsca zdarzenia
        DistanceIterator iterator = new DistanceIterator(allFireStations, incident.getLatitude(), incident.getLongitude());

        // jeśli jest dostępna kolejna stacja i nie ma zadysponowanych do zdarzenia wszystkich
        // potrzebnych wozów, to pętla działa dalej
        while(iterator.hasNext() && dispatchedTrucks < requiredTrucks) {
            FireStation currentFireStation = iterator.next(); // następna dostępna stacja
            List<Truck> availableTrucks = currentFireStation.getAvailableTrucks(); // lista dostępnych wozów

            // iterowanie po dostępnych wozach
            for(Truck truck : availableTrucks) {
                if(dispatchedTrucks < requiredTrucks) {
                    // zadysponowanie kolejnego wozu
                    truck.setTruckState(new EnRouteState());
                    truck.setTargetIncident(incident);
                    SimulationLogger.addLog("Zadysponowano 🚒 " + truck.getTruckId() + " ze stacji 🏢 " + currentFireStation.getName());
                    dispatchedTrucks++;
                }
            }
        }

        if(dispatchedTrucks < requiredTrucks) 
            SimulationLogger.addLog("UWAGA: Nie znaleziono wystarczającej liczby wolnych wozów! Wysłano: " + dispatchedTrucks + "/" + requiredTrucks);
        else {
            SimulationLogger.addLog("Sukces: Zadysponowano komplet " + dispatchedTrucks + " wozów.");
        }
    }

    
    // gettery i settery
    public void setIncidentStrategy(IIncidentStrategy strategy) {
        this.incidentStrategy = strategy;
    }


}
