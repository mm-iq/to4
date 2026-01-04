package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import Models.*;
import States.*;

public class MapPanel extends JPanel {

    private List<FireStation> stations;
    private List<Incident> incidents;

    // Granice mapy Krakowa (przybliżone)
    // Dzięki temu wiemy, jak przeskalować GPS na piksele
    private final double MIN_LAT = 49.97;
    private final double MAX_LAT = 50.10;
    private final double MIN_LON = 19.80;
    private final double MAX_LON = 20.05;

    public MapPanel(List<FireStation> stations, List<Incident> incidents) {
        this.stations = stations;
        this.incidents = incidents;
        this.setBackground(new Color(30, 30, 30)); // Ciemne tło, wygląda profesjonalnie
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        
        // Włączenie wygładzania krawędzi (antyaliasing) - ładniejsza grafika
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // 1. RYSOWANIE STACJI
        for (FireStation fs : stations) {
            int x = convertLonToX(fs.getLongitude(), w);
            int y = convertLatToY(fs.getLatitude(), h);

            g2.setColor(Color.CYAN);
            g2.fillRect(x - 5, y - 5, 10, 10); // Kwadracik stacji
            g2.drawString(fs.getName(), x + 8, y + 5); // Podpis
        }

        // 2. RYSOWANIE ZDARZEŃ (POŻARÓW)
        // Zakładamy, że interesuje nas ostatnie aktywne zdarzenie jako cel
        Incident targetIncident = incidents.isEmpty() ? null : incidents.get(incidents.size() - 1);

        for (Incident inc : incidents) {
            int x = convertLonToX(inc.getLongitude(), w);
            int y = convertLatToY(inc.getLatitude(), h);

            g2.setColor(Color.RED);
            // Pulsujący efekt (rysowanie otoczki)
            g2.fillOval(x - 6, y - 6, 12, 12);
            g2.setColor(new Color(255, 0, 0, 100)); // Przezroczysty
            g2.fillOval(x - 12, y - 12, 24, 24);
            
            g2.setColor(Color.WHITE);
            g2.drawString(inc.getType().toString(), x + 10, y);
        }

        // 3. RYSOWANIE I ANIMACJA WOZÓW
        for (FireStation fs : stations) {
            for (Truck truck : fs.getAllTrucks()) {
                drawTruck(g2, truck, fs, targetIncident, w, h);
            }
        }
    }

    private void drawTruck(Graphics2D g2, Truck truck, FireStation station, Incident target, int w, int h) {
        int startX = convertLonToX(station.getLongitude(), w);
        int startY = convertLatToY(station.getLatitude(), h);
        
        int currentX = startX;
        int currentY = startY;
        Color truckColor = Color.GREEN;

        // Logika animacji
        // Jeśli wóz jedzie, musimy obliczyć gdzie jest "pomiędzy" stacją a pożarem
        if (target != null && (truck.getTruckState() instanceof EnRouteState || truck.getTruckState() instanceof ReturningState)) {
            
            int targetX = convertLonToX(target.getLongitude(), w);
            int targetY = convertLatToY(target.getLatitude(), h);
            
            // Symulujemy czas podróży (zakładamy max 4 sekundy, bo tak masz w stanach)
            double progress = (double) truck.getElapsedTime() / 4.0; 
            if (progress > 1.0) progress = 1.0;

            if (truck.getTruckState() instanceof EnRouteState) {
                // Jedzie DO pożaru
                truckColor = Color.YELLOW;
                currentX = startX + (int)((targetX - startX) * progress);
                currentY = startY + (int)((targetY - startY) * progress);
                
                // Rysujemy linię trasy
                g2.setColor(new Color(255, 255, 0, 50));
                g2.drawLine(startX, startY, targetX, targetY);
            } 
            else if (truck.getTruckState() instanceof ReturningState) {
                // Wraca DO bazy
                truckColor = Color.BLUE;
                // Odwracamy logikę: startujemy od celu, idziemy do bazy
                currentX = targetX + (int)((startX - targetX) * progress);
                currentY = targetY + (int)((startY - targetY) * progress);
            }
        } 
        else if (truck.getTruckState() instanceof ActionState) {
            // Wóz jest na miejscu akcji
            if (target != null) {
                currentX = convertLonToX(target.getLongitude(), w);
                currentY = convertLatToY(target.getLatitude(), h);
            }
            truckColor = Color.RED;
        }

        // Rysowanie kropki wozu
        g2.setColor(truckColor);
        g2.fillOval(currentX - 3, currentY - 3, 6, 6);
    }

    // --- MATEMATYKA SKALOWANIA (GPS -> PIKSELE) ---
    
    private int convertLonToX(double lon, int width) {
        return (int) ((lon - MIN_LON) / (MAX_LON - MIN_LON) * width);
    }

    // Uwaga: Oś Y na ekranie rośnie w dół, a szerokość geograficzna rośnie w górę, dlatego odwracamy
    private int convertLatToY(double lat, int height) {
        return (int) (height - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * height));
    }
}