package Visualization;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import Models.*;
import States.*;

public class MapPanel extends JPanel {

    private List<FireStation> stations;
    private List<Incident> incidents;

    // Twoje sprawdzone współrzędne z repozytorium
    private final double MIN_LAT = 49.94;
    private final double MAX_LAT = 50.16;
    private final double MIN_LON = 19.67;
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
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));

        int w = getWidth();
        int h = getHeight();

        // 1. RYSOWANIE STACJI
        for (int i = 0; i < stations.size(); i++) {
            FireStation fs = stations.get(i);
            int x = convertLonToX(fs.getLongitude(), w);
            int y = convertLatToY(fs.getLatitude(), h);

            g2.setColor(Color.CYAN);
            g2.fillRect(x - 5, y - 5, 10, 10);
            
            long available = fs.getAvailableTrucks().size();
            long total = fs.getAllTrucks().size();
            String label = fs.getName() + " [" + available + "/" + total + "]";

            // Rozsuwanie napisów stacji, żeby na siebie nie wchodziły
            int yOffset = -10; 
            switch (i) {
                case 1: // JRG-2
                case 2: // JRG-3
                case 5: // JRG-6
                case 8: // JRG-ASPIRANCI
                    yOffset = 20; 
                    break;
                default:
                    yOffset = -10;
                    break;
            }

            int stringWidth = g2.getFontMetrics().stringWidth(label);
            g2.drawString(label, x - (stringWidth / 2), y + yOffset);
        }

        // 2. RYSOWANIE ZDARZEŃ
        for (Incident inc : incidents) {
            int x = convertLonToX(inc.getLongitude(), w);
            int y = convertLatToY(inc.getLatitude(), h);

            g2.setColor(Color.RED);
            g2.fillOval(x - 6, y - 6, 12, 12);
            g2.setColor(new Color(255, 0, 0, 100));
            g2.fillOval(x - 12, y - 12, 24, 24);
            
            g2.setColor(Color.WHITE);
            g2.drawString(inc.getType().toString(), x + 10, y + 5);
        }

        // 3. RYSOWANIE WOZÓW
        for (FireStation fs : stations) {
            for (Truck truck : fs.getAllTrucks()) {
                drawTruck(g2, truck, fs, w, h);
            }
        }
    }

    // Visualization/MapPanel.java

private void drawTruck(Graphics2D g2, Truck truck, FireStation station, int w, int h) {
    int stationX = convertLonToX(station.getLongitude(), w);
    int stationY = convertLatToY(station.getLatitude(), h);
    
    int currentX = stationX;
    int currentY = stationY;
    Color truckColor = Color.GREEN;

    Incident target = truck.getTargetIncident();
    
    // Obliczanie czasu i postępu (tak jak ustaliliśmy wcześniej)
    int timeRemaining = truck.getTruckState().getTimeRemaining(truck);
    int elapsedTime = truck.getElapsedTime();
    int totalDuration = elapsedTime + timeRemaining;

    double progress = (totalDuration == 0) ? 1.0 : (double) elapsedTime / totalDuration;
    if (progress > 1.0) progress = 1.0;

    // --- RYSOWANIE ---

    // 1. DOJAZD (EnRouteState)
    if (target != null && truck.getTruckState() instanceof EnRouteState) {
        int targetX = convertLonToX(target.getLongitude(), w);
        int targetY = convertLatToY(target.getLatitude(), h);
        
        truckColor = Color.YELLOW;
        // Pozycja na linii
        int lineX = stationX + (int)((targetX - stationX) * progress);
        int lineY = stationY + (int)((targetY - stationY) * progress);
        
        // Rysujemy "duchową" linię trasy
        g2.setColor(new Color(255, 255, 0, 100));
        g2.drawLine(stationX, stationY, targetX, targetY);
        
        // Obliczamy przesunięcie (żeby wozy nie jechały idealnie jeden na drugim)
        int offset = (truck.getTruckId() % 3) * 5; 
        
        // AKTUALIZUJEMY currentX/Y o przesunięcie - teraz to jest faktyczna pozycja wozu
        currentX = lineX + offset;
        currentY = lineY + offset;

        g2.setColor(truckColor);
        g2.fillOval(currentX - 3, currentY - 3, 6, 6);
    } 
    // 2. POWRÓT (ReturningState)
    else if (target != null && truck.getTruckState() instanceof ReturningState) {
        int targetX = convertLonToX(target.getLongitude(), w);
        int targetY = convertLatToY(target.getLatitude(), h);

        truckColor = Color.ORANGE;
        // Pozycja na linii (od celu do stacji)
        int lineX = targetX + (int)((stationX - targetX) * progress);
        int lineY = targetY + (int)((stationY - targetY) * progress);
        
        g2.setColor(new Color(255, 165, 0, 100));
        g2.drawLine(targetX, targetY, stationX, stationY);

        // Przesunięcie wizualne
        int offset = (truck.getTruckId() % 3) * 5;

        // AKTUALIZUJEMY currentX/Y - tekst podąży za przesunięciem
        currentX = lineX + offset;
        currentY = lineY + offset;

        g2.setColor(truckColor);
        g2.fillOval(currentX - 3, currentY - 3, 6, 6);
    }
    // 3. W AKCJI (ActionState)
    else if (truck.getTruckState() instanceof ActionState && target != null) {
        int centerX = convertLonToX(target.getLongitude(), w);
        int centerY = convertLatToY(target.getLatitude(), h);
        
        g2.setColor(Color.RED);
        
        // Rozłożenie wozów na okręgu
        int angle = (truck.getTruckId() * 45); 
        // Zwiększyłem dystans z 12 na 20, żeby liczniki na siebie nie właziły
        int dist = 20; 
        int offsetX = (int)(Math.cos(Math.toRadians(angle)) * dist);
        int offsetY = (int)(Math.sin(Math.toRadians(angle)) * dist);
        
        // AKTUALIZUJEMY currentX/Y - teraz wskazują na kropkę na obwodzie, a nie środek
        currentX = centerX + offsetX;
        currentY = centerY + offsetY;
        
        g2.fillOval(currentX - 3, currentY - 3, 6, 6);
    }
    // 4. WOLNY (IdleState) - opcjonalnie, jeśli chcesz widzieć wozy w stacji
    else {
        // Jeśli wóz jest w bazie, też można go lekko przesunąć, żeby nie zlewał się ze stacją
        // Ale zazwyczaj licznika czasu wtedy nie ma, więc nie jest to krytyczne.
        // currentX/Y są już ustawione na stationX/Y
    }

    // --- RYSOWANIE LICZNIKA CZASU PRZY WOZIE ---
    if (timeRemaining > 0) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10)); 
        
        // Rysujemy względem zaktualizowanego currentX/Y (pozycja kropki)
        // Przesuwamy napis o +8px w prawo i -5px w górę, żeby nie zasłaniał kropki
        g2.drawString(String.valueOf(timeRemaining) + "s", currentX + 8, currentY - 5);
        
        g2.setFont(new Font("SansSerif", Font.BOLD, 11)); 
    }
}



    private int convertLonToX(double lon, int width) {
        return (int) ((lon - MIN_LON) / (MAX_LON - MIN_LON) * width);
    }

    private int convertLatToY(double lat, int height) {
        return (int) (height - ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * height));
    }
}