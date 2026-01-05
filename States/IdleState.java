package States;

import Models.*;

public class IdleState implements ITruckState {

    @Override
    public void update(Truck truck) {
    }

    @Override
    public String toString() {
        return "WOLNY";
    }

    @Override
    public int getTimeRemaining(Truck truck) {
        return 0;
    }
}