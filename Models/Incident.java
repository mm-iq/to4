package Models;
public class Incident {
    

    public enum IncidentType {
        POZAR,
        MIEJSCOWE_ZDARZENIE,
    };

    private IncidentType type;
    private double latitude;
    private double longitude;

    public Incident(IncidentType type, double latitude, double longitude) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // gettery
    public IncidentType getType() { return type; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

}
 