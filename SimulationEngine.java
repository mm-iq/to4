import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import App.*;
import Models.*;
import States.ActionState;
import States.EnRouteState;
import States.IdleState;
import States.ReturningState;

public class SimulationEngine {

    private List<FireStation> fireStations = new ArrayList<>();
    private List<Incident> activeIncidents = new ArrayList<>();
    private DispatchCenter skkm = new DispatchCenter();
    private Random random = new Random();

    private int second = 0;
    
    public void setup() {
        
        // JRG-1 (Centrum/Westerplatte)
        FireStation jrg1 = new FireStation("JRG-1_CENTRUM", 50.0633, 19.9433);
        
        // JRG-2 (Podgórze/Rzemieślnicza)
        FireStation jrg2 = new FireStation("JRG-2_PODGÓRZE", 50.0344, 19.9362);
        
        // JRG-3 (Krowodrza/Zarzecze)
        FireStation jrg3 = new FireStation("JRG-3_KROWODRZA", 50.0763, 19.8927);
        
        // JRG-4 (Nowa Huta/Obrońców Modlina)
        FireStation jrg4 = new FireStation("JRG-4-NOWA_HUTA", 50.0345, 20.0169);
        
        // JRG-5 (Azory/Wyki)
        FireStation jrg5 = new FireStation("JRG-5_AZORY", 50.0911, 19.9167);
        
        // JRG-6 (Prokocim/Aleksandry)
        FireStation jrg6 = new FireStation("JRG-6_PROKOCIM", 50.0151, 20.0071);
        
        // JRG-7 (Prądnik/Rozrywka)
        FireStation jrg7 = new FireStation("JRG-7_PRĄDNIK", 50.0937, 19.9863);
        
        // JRG Skawina (Piłsudskiego)
        FireStation jrg8 = new FireStation("JRG-SKAWINA", 49.9722, 19.8153);
        
        // Szkoła Aspirantów (Nowa Huta/Os. Zgody)
        FireStation jrg9 = new FireStation("JRG-ASPIRANCI", 50.0739, 20.0374);
        
        // Lotniskowa Straż Pożarna (Balice)
        FireStation jrg10 = new FireStation("LSP-BALICE", 50.0777, 19.7848);
            
        fireStations.add(jrg1);
        fireStations.add(jrg2);
        fireStations.add(jrg3);
        fireStations.add(jrg4);
        fireStations.add(jrg5);
        fireStations.add(jrg6);
        fireStations.add(jrg7);
        fireStations.add(jrg8);
        fireStations.add(jrg9);
        fireStations.add(jrg10);

        // dodanie wozów
        for(FireStation fireStation : fireStations) {
            for(int i=0; i<5; i++) fireStation.addTruck(new Truck(fireStation));
            SimulationLogger.addLog("Dodałem wozy do " + fireStation.getName());

            skkm.addObserver(fireStation);
            SimulationLogger.addLog("Dodałem stację " + fireStation.getName() + " do listy obserwatorów.");
        }
    }

    public void tick() {

        second++;

        if(second%10 == 0) {
            Incident newIncident = drawIncident();
            activeIncidents.add(newIncident);
            SimulationLogger.addLog("🚨 Nowe zdarzenie: " + newIncident.getType());
            skkm.handleIncident(newIncident, fireStations);
        }

        // aktualizacja wozów
        for(FireStation fireStation : fireStations) {
            for(Truck truck : fireStation.getAvailableTrucks() == null ? new ArrayList<Truck>() : getAllTrucksFromStation(fireStation)){
                truck.updateTruckState();

                if (truck.getTruckState() instanceof IdleState) {
                    truck.setTargetIncident(null);
                }
            }
        }

        cleanupIncident();
    }

    private void cleanupIncident() {
        Iterator<Incident> it = activeIncidents.iterator();
        while(it.hasNext()) {
            Incident incident = it.next();

            boolean isStillActive = false;

            // sprawdzamy czy jest jakiś wóz, który jedzie do tego zdarzenia lub bierze udział w akcji
            for(FireStation fs : fireStations) {
                for(Truck t : fs.getAllTrucks()) {
                    if(t.getTargetIncident() == incident) {
                        if( t.getTruckState() instanceof EnRouteState || 
                            t.getTruckState() instanceof ActionState ||
                            t.getTruckState() instanceof ReturningState) {
                            isStillActive = true;
                        }
                    }
                }
            }

            // jeśli żaden wóz nie zajmuje się tym zdarzeniem, czyli wraca lub jest wolny, usuwamy je
            if(!isStillActive) {
                it.remove();
            }
        }
    }

    public Incident drawIncident() {

        Incident.IncidentType type;
        
        // losowanie rodzaju zdarzenia 30% pożary, 70% miejscowe zdarzenia
        if(random.nextDouble() < 0.3) {
            type = Incident.IncidentType.POZAR;
        }
        else {
            type = Incident.IncidentType.MIEJSCOWE_ZDARZENIE;
        }


        double randomLatitude = 49.95855 + (50.15456 - 49.95855) * random.nextDouble();
        double randomLongitude = 19.68829 + (20.02470 - 19.68829) * random.nextDouble();

        return new Incident(type, randomLatitude, randomLongitude);
    }

    public static void main(String[] args) {
        
        SimulationEngine engine = new SimulationEngine();
        engine.setup();
    }
    
    // gettery i settery

    public List<Truck> getAllTrucksFromStation(FireStation fireStation) {
        return fireStation.getAllTrucks();
    }

    public List<Truck> getAllTrucksFromAllStationsAsOneList() {
        List<Truck> allTrucks = new ArrayList<>();
        for(FireStation fireStation : this.fireStations) {
            allTrucks.addAll(fireStation.getAllTrucks());
        }
        return allTrucks;
    }

    public List<FireStation> getFireStations() {
        return this.fireStations;
    }

    public List<Incident> getActiveIncidents() {
        return this.activeIncidents;
    }
}
