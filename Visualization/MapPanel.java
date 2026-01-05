package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import Models.*;
import States.*;

public class MapPanel extends JPanel {

    private List<FireStation> stations;
    private List<Incident> incidents;

    // Granice mapy (z marginesem, żeby objąć Balice i Skawinę)
    private final double MIN_LAT = 49.94;
    private final double MAX_LAT = 50.13;
    private final double MIN_LON = 19.75;
    private final double MAX_LON = 20.15;

    public MapPanel(List<FireStation> stations, List<Incident> incidents) {
        this.stations = stations;
        this.incidents = incidents;
        this.setBackground(new Color(30, 30, 30));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Wygładzanie krawędzi
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Ustawienie mniejszej, czytelniejszej czcionki
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));

        int w = getWidth();
        int h = getHeight();

        // 1. RYSOWANIE STACJI
        // Używamy pętli for z indeksem 'i', aby móc przesuwać napisy naprzemiennie
        for (int i = 0; i < stations.size(); i++) {
            FireStation fs = stations.get(i);
            int x = convertLonToX(fs.getLongitude(), w);
            int y = convertLatToY(fs.getLatitude(), h);

            // Rysowanie kwadratu stacji
            g2.setColor(Color.CYAN);
            g2.fillRect(x - 5, y - 5, 10, 10);
            
            // Przygotowanie tekstu
            long available = fs.getAvailableTrucks().size();
            long total = fs.getAllTrucks().size();
            String label = fs.getName() + " [" + available + "/" + total + "]";

            // --- LOGIKA ANTY-KOLIZYJNA ---
            // Jeśli indeks jest parzysty -> napis NAD stacją
            // Jeśli nieparzysty -> napis POD stacją
            // To zapobiega nachodzeniu na siebie nazw bliskich stacji (np. JRG1 i JRG2)
            int yOffset;
            if (i % 2 == 0) {
                yOffset = -10; // Przesunięcie w górę
            }
            else if(i == 3) {
                yOffset = -10;
            } 
            else {
                yOffset = 20;  // Przesunięcie w dół
            }

            g2.drawString(label, x - 10, y + yOffset);
        }

        // 2. RYSOWANIE ZDARZEŃ
        for (Incident inc : incidents) {
            int x = convertLonToX(inc.getLongitude(), w);
            int y = convertLatToY(inc.getLatitude(), h);

            g2.setColor(Color.RED);
            g2.fillOval(x - 6, y - 6, 12, 12);
            // Efekt "pulsu" (półprzezroczysta otoczka)
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillOval(x - 12, y - 12, 24, 24);
            
            g2.setColor(Color.WHITE);
            g2.drawString(inc.getType().toString(), x + 10, y + 5);
        }

        // 3. RYSOWANIE I ANIMACJA WOZÓW
        for (FireStation fs : stations) {
            for (Truck truck : fs.getAllTrucks()) {
                drawTruck(g2, truck, fs, w, h);
            }
        }
    }

    private void drawTruck(Graphics2D g2, Truck truck, FireStation station, int w, int h) {
        int stationX = convertLonToX(station.getLongitude(), w);
        int stationY = convertLatToY(station.getLatitude(), h);
        
        int currentX = stationX;
        int currentY = stationY;
        Color truckColor = Color.GREEN;

        Incident target = truck.getTargetIncident();
        
        // --- LOGIKA DOJAZDU (ŻÓŁTA LINIA) ---
        if (target != null && truck.getTruckState() instanceof EnRouteState) {
            int targetX = convertLonToX(target.getLongitude(), w);
            int targetY = convertLatToY(target.getLatitude(), h);
            
            double progress = (double) truck.getElapsedTime() / 4.0;
            if (progress > 1.0) progress = 1.0;

            truckColor = Color.YELLOW;
            currentX = stationX + (int)((targetX - stationX) * progress);
            currentY = stationY + (int)((targetY - stationY) * progress);
            
            g2.setColor(new Color(255, 255, 0, 100)); // Półprzezroczysta linia
            g2.drawLine(stationX, stationY, targetX, targetY);
            
            g2.setColor(truckColor);
            // Małe przesunięcie wozów względem siebie, żeby nie jechały "gęsiego" w jednym punkcie
            int offset = (truck.getTruckId() % 3) * 4; 
            g2.fillOval(currentX - 3 + offset, currentY - 3 + offset, 6, 6);
        } 
        // --- LOGIKA POWROTU (POMARAŃCZOWA LINIA) ---
        else if (target != null && truck.getTruckState() instanceof ReturningState) {
            int targetX = convertLonToX(target.getLongitude(), w);
            int targetY = convertLatToY(target.getLatitude(), h);

            double progress = (double) truck.getElapsedTime() / 4.0; 
            if (progress > 1.0) progress = 1.0;

            truckColor = Color.ORANGE;
            // Odwracamy wektor: od celu do bazy
            currentX = targetX + (int)((stationX - targetX) * progress);
            currentY = targetY + (int)((stationY - targetY) * progress);
            
            g2.setColor(new Color(255, 165, 0, 100));
            g2.drawLine(targetX, targetY, stationX, stationY);

            g2.setColor(truckColor);
            int offset = (truck.getTruckId() % 3) * 4;
            g2.fillOval(currentX - 3 + offset, currentY - 3 + offset, 6, 6);
        }
        // --- LOGIKA W AKCJI ---
        else if (truck.getTruckState() instanceof ActionState && target != null) {
            currentX = convertLonToX(target.getLongitude(), w);
            currentY = convertLatToY(target.getLatitude(), h);
            
            g2.setColor(Color.RED);
            // Rozrzucamy kropki wokół pożaru
            int angle = (truck.getTruckId() * 45); 
            int dist = 12;
            int offsetX = (int)(Math.cos(Math.toRadians(angle)) * dist);
            int offsetY = (int)(Math.sin(Math.toRadians(angle)) * dist);
            
            g2.fillOval(currentX + offsetX - 3, currentY + offsetY - 3, 6, 6);
        }
    }

    // --- MATEMATYKA SKALOWANIA ---
    private int convertLonToX(double lon, int width) {
        return (int) ((lon - MIN_LON) / (MAX_LON - MIN_LON) * width);
    }

    private int convertLatToY(double lat, int height) {
        return (int) (height - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * height));
    }
}