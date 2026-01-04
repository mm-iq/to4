package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import Models.*;
import States.*;

public class MapPanel extends JPanel {

    private List<FireStation> stations;
    private List<Incident> incidents;

    // Granice mapy
    private final double MIN_LAT = 49.97;
    private final double MAX_LAT = 50.10;
    private final double MIN_LON = 19.80;
    private final double MAX_LON = 20.05;

    public MapPanel(List<FireStation> stations, List<Incident> incidents) {
        this.stations = stations;
        this.incidents = incidents;
        this.setBackground(new Color(30, 30, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. RYSOWANIE STACJI Z LICZNIKIEM
        for (FireStation fs : stations) {
            int x = convertLonToX(fs.getLongitude(), w);
            int y = convertLatToY(fs.getLatitude(), h);

            g2.setColor(Color.CYAN);
            g2.fillRect(x - 5, y - 5, 10, 10);
            
            // Liczenie dostępnych wozów
            long available = fs.getAvailableTrucks().size();
            long total = fs.getAllTrucks().size();
            
            // Wyświetlanie nazwy i stanu (np. "JRG-1 [3/5]")
            g2.drawString(fs.getName() + " [" + available + "/" + total + "]", x + 8, y + 5);
        }

        // 2. RYSOWANIE ZDARZEŃ
        // Rysujemy tylko te, które są na liście (te "usunięte" znikną)
        for (Incident inc : incidents) {
            int x = convertLonToX(inc.getLongitude(), w);
            int y = convertLatToY(inc.getLatitude(), h);

            g2.setColor(Color.RED);
            g2.fillOval(x - 6, y - 6, 12, 12);
            g2.setColor(new Color(255, 0, 0, 100)); // Przezroczysty puls
            g2.fillOval(x - 12, y - 12, 24, 24);
            
            g2.setColor(Color.WHITE);
            g2.drawString(inc.getType().toString(), x + 10, y);
        }

        // 3. RYSOWANIE I ANIMACJA WOZÓW
        for (FireStation fs : stations) {
            for (Truck truck : fs.getAllTrucks()) {
                drawTruck(g2, truck, fs, w, h);
            }
        }
    }

    private void drawTruck(Graphics2D g2, Truck truck, FireStation station, int w, int h) {
        // Pozycja startowa (stacja)
        int stationX = convertLonToX(station.getLongitude(), w);
        int stationY = convertLatToY(station.getLatitude(), h);
        
        int currentX = stationX;
        int currentY = stationY;
        Color truckColor = Color.GREEN;

        // Pobieramy cel wozu (jeśli ma)
        Incident target = truck.getTargetIncident();
        
        // --- LOGIKA DOJAŻDŻU (ŻÓŁTA LINIA) ---
        if (target != null && truck.getTruckState() instanceof EnRouteState) {
            int targetX = convertLonToX(target.getLongitude(), w);
            int targetY = convertLatToY(target.getLatitude(), h);
            
            double progress = (double) truck.getElapsedTime() / 4.0; // max 4 sek
            if (progress > 1.0) progress = 1.0;

            truckColor = Color.YELLOW;
            currentX = stationX + (int)((targetX - stationX) * progress);
            currentY = stationY + (int)((targetY - stationY) * progress);
            
            // Żółta linia przerywana lub po prostu półprzezroczysta
            g2.setColor(new Color(255, 255, 0, 100));
            g2.drawLine(stationX, stationY, targetX, targetY);
            
            // Rysujemy "kropkę" wozu
            g2.setColor(truckColor);
            // Dodajemy mały offset bazujący na ID, żeby wozy nie nakładały się idealnie na siebie
            int offset = (truck.getTruckId() % 3) * 3; 
            g2.fillOval(currentX - 3 + offset, currentY - 3 + offset, 6, 6);
        } 
        // --- LOGIKA POWROTU (POMARAŃCZOWA LINIA) ---
        else if (target != null && truck.getTruckState() instanceof ReturningState) {
            int targetX = convertLonToX(target.getLongitude(), w);
            int targetY = convertLatToY(target.getLatitude(), h);

            double progress = (double) truck.getElapsedTime() / 4.0; 
            if (progress > 1.0) progress = 1.0;

            truckColor = Color.ORANGE;
            // Odwracamy: startujemy od celu (target), idziemy do bazy (station)
            currentX = targetX + (int)((stationX - targetX) * progress);
            currentY = targetY + (int)((stationY - targetY) * progress);
            
            // Pomarańczowa linia
            g2.setColor(new Color(255, 165, 0, 100));
            g2.drawLine(targetX, targetY, stationX, stationY);

            g2.setColor(truckColor);
            int offset = (truck.getTruckId() % 3) * 3;
            g2.fillOval(currentX - 3 + offset, currentY - 3 + offset, 6, 6);
        }
        // --- LOGIKA W AKCJI (CZERWONA KROPKA NA MIEJSCU) ---
        else if (truck.getTruckState() instanceof ActionState && target != null) {
            currentX = convertLonToX(target.getLongitude(), w);
            currentY = convertLatToY(target.getLatitude(), h);
            
            g2.setColor(Color.RED);
            // Rozrzucamy kropki wokół zdarzenia, żeby było widać ile ich jest
            // Używamy funkcji trygonometrycznych lub prostego offsetu
            int angle = (truck.getTruckId() * 45); 
            int dist = 10;
            int offsetX = (int)(Math.cos(Math.toRadians(angle)) * dist);
            int offsetY = (int)(Math.sin(Math.toRadians(angle)) * dist);
            
            g2.fillOval(currentX + offsetX - 3, currentY + offsetY - 3, 6, 6);
        }
    }

    private int convertLonToX(double lon, int width) {
        return (int) ((lon - MIN_LON) / (MAX_LON - MIN_LON) * width);
    }

    private int convertLatToY(double lat, int height) {
        return (int) (height - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * height));
    }
}