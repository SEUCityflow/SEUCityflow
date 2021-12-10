package entity.vehicle.laneChange;

import entity.roadNet.roadNet.Lane;
import entity.vehicle.vehicle.Vehicle;

public class Signal {
    private int urgency;
    private int direction;
    private Lane target;
    private Vehicle source;
    //    int response;
//    double extraSpace;

    public Signal() {
    }

    Signal(Signal other) {
        urgency = other.urgency;
        direction = other.direction;
        target = other.target;
        source = other.source;
    }

    public int getUrgency() {
        return urgency;
    }

    public void setUrgency(int urgency) {
        this.urgency = urgency;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public Lane getTarget() {
        return target;
    }

    public void setTarget(Lane target) {
        this.target = target;
    }

    public Vehicle getSource() {
        return source;
    }

    public void setSource(Vehicle source) {
        this.source = source;
    }
}
