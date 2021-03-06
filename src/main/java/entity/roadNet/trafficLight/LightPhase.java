package entity.roadNet.trafficLight;

import java.util.ArrayList;
import java.util.List;

public class LightPhase {
    private double time;
    private List<Boolean> roadLinkAvailable;

    public LightPhase() {
        roadLinkAvailable = new ArrayList<>();
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public List<Boolean> getRoadLinkAvailable() {
        return roadLinkAvailable;
    }

    public void setRoadLinkAvailable(List<Boolean> roadLinkAvailable) {
        this.roadLinkAvailable = roadLinkAvailable;
    }
}