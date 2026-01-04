package App;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import Models.FireStation;

public class DistanceIterator implements Iterator<FireStation> {

    private List<FireStation> sortedFireStations; // lista jednostek
    private int position = 0; // pozycja

    public DistanceIterator(List<FireStation> stations, double incidentLatitude, double incidentLongitude) {
        
        // kopia, aby nie zmieniać oryginału
        this.sortedFireStations = new ArrayList<>(stations);

        // sortowanie stacji wg najmniejszego dystansu od miejsca zdarzenia
        Collections.sort(
            sortedFireStations, 
            new Comparator<FireStation>() {
                @Override
                public int compare(FireStation fs1, FireStation fs2) {
                    double d1 = fs1.calculateDistance(incidentLatitude, incidentLongitude);
                    double d2 = fs2.calculateDistance(incidentLatitude, incidentLongitude);
                    return Double.compare(d1, d2);
                }
            }
        );
    }

    @Override
    public boolean hasNext() {
        return position < sortedFireStations.size();
    }

    @Override
    public FireStation next() {
        return sortedFireStations.get(position++);
    }
}
