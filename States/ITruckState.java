package States;

import Models.Truck;

public interface ITruckState {
    void update(Truck truck);
    int getTimeRemaining(Truck truck);
}

