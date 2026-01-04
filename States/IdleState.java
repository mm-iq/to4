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
}