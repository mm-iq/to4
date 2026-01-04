import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import App.*;
import Models.*;

public class SimulationEngine {

    private List<FireStation> fireStations = new ArrayList<>();
    private List<Incident> activeIncidents = new ArrayList<>();
    private DispatchCenter skkm = new DispatchCenter();
    private Random random = new Random();
    
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
            System.out.println("Dodałem wozy do stacji: " + fireStation.getName());

            skkm.addObserver(fireStation);
            System.out.println("Dodałem stację: " + fireStation.getName() + " do listy obserwatorów.");
        }
    }
    
    public void start() {
        
        System.out.println("Symulacja została uruchomiona...");
        int second = 0;

        while(true) {
            second++;
            System.out.println("\n--- SEKUNDA " + second + " ---");
            
            // losowe zdarzenie co 10 sekund
            if(second % 5 == 0) {
                
                printDashboard(second);
                Incident newIncident = drawIncident();
                activeIncidents.add(newIncident);
                System.out.println("!!! NOWE ZDARZENIE: " + newIncident.getType());
                skkm.handleIncident(newIncident, fireStations);
            }

            List<String> logs = SimulationLogger.consumeLogs();
            for(String log : logs) {
                System.out.println(log);
            }


            // aktualizacja wozów
            for(FireStation fireStation : fireStations) {
                for(Truck truck : fireStation.getAvailableTrucks() == null ? new ArrayList<Truck>() : getAllTrucksFromStation(fireStation)){
                    truck.updateTruckState();
                }
            }

            try { Thread.sleep(1000); } catch (InterruptedException e) { break; }
            
        // // po wylosowaniu Incident wywołujemy powiadomienie DispatchCenter.notify
        // System.out.println("---GENEROWANIE ZDARZENIA---");
        // activeIncidents.add(new Incident(Incident.IncidentType.PZ, 50.050, 19.950));
    
        // // każda jednosta dostaje w update() powiadomienie i sprawdza dostępne wozy
        // System.out.println("---POWIADAMIAM JEDNOSTKI---");
        // skkm.notifyObservers(activeIncidents.get(0));
        }
    }

    public Incident drawIncident() {

        int randomTypeIndex = random.nextInt(2);
        double randomLatitude = 49.95855 + (50.15456 - 49.95855) * random.nextDouble();
        double randomLongitude = 19.68829 + (20.02470 - 19.68829) * random.nextDouble();

        Incident incident = new Incident(Incident.IncidentType.values()[randomTypeIndex], randomLatitude, randomLongitude);

        return incident;
    }

    private void printDashboard(int second) {
        // --- CZYSZCZENIE KONSOLI ---
        System.out.print("\033[H\033[2J");  
        System.out.flush();
    
        // --- NAGŁÓWEK ---
        System.out.println( "╔════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   SYMULACJA SKKM - CZAS: " + String.format("%04d", second) + " s                 ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════╝" );


        System.out.println(""); 

        // --- TABELA STANU WOZÓW ---
        Incident currentIncident = activeIncidents.isEmpty() ? null : activeIncidents.get(activeIncidents.size() - 1);

        for (FireStation station : fireStations) {
            
            // Obliczamy dystans, jeśli jest aktywny incydent
            String distanceInfo = "";
            if (currentIncident != null) {
                double dist = station.calculateDistance(currentIncident.getLatitude(), currentIncident.getLongitude());
                distanceInfo = " | DYSTANS: " + String.format("%.3f", dist); 
            }
    
            System.out.println( "📍 " + station.getName() +  distanceInfo); 
            System.out.println("---------------------------------------------------------------");
            System.out.println(String.format("| %-5s | %-20s | %-15s |", "ID", "STAN", "CZAS TRWANIA"));
            System.out.println("---------------------------------------------------------------");
    
            for (Truck truck : station.getAllTrucks()) {
                // String color = ANSI_GREEN;
                String stateName = "WOLNY";
                
                // Dobieramy kolor i nazwę zależnie od stanu
                if (truck.getTruckState() instanceof States.ActionState) {
                    // color = ANSI_RED;
                    stateName = "W AKCJI 🚨";
                } else if (truck.getTruckState() instanceof States.EnRouteState) {
                    // color = ANSI_YELLOW;
                    stateName = "DOJAZD 🚒";
                } else if (truck.getTruckState() instanceof States.ReturningState) {
                    // color = ANSI_BLUE;
                    stateName = "POWRÓT ↩";
                }
    
                // Rysujemy wiersz tabeli
                System.out.println(String.format("| %-5d | %-20s | %-15d |",
                    truck.getTruckId(),
                    stateName,
                    truck.getElapsedTime()
                ));
            }
            System.out.println("---------------------------------------------------------------");
        }
    }

    public static void main(String[] args) {
        
        SimulationEngine engine = new SimulationEngine();
        engine.setup();
        engine.start();
    }
    
    // gettery i settery

    public List<Truck> getAllTrucksFromStation(FireStation fireStation) {
        return fireStation.getAllTrucks();
    }
}
