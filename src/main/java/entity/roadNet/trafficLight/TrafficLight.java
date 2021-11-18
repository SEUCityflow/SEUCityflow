package entity.roadNet.trafficLight;

import entity.roadNet.roadNet.Intersection;

import java.util.List;

class LightPhase {
    private int phase;
    private double time;
    private List<Boolean> roadLinkAvailable;

    public int getPhase() {
        return phase;
    }

    public void setPhase(int phase) {
        this.phase = phase;
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

public class TrafficLight {
    private Intersection intersection;
    private List<LightPhase> phases;
    private List<Integer> roadLinkIndices;
    private double remainDuration;
    private int curPhaseIndex;

    public void init(int initPhaseIndex) {

    }

    public int getCurrentPhaseIndex() {
        return 0;
    }

    public LightPhase getCurrentPhase() {
        return null;
    }

    public Intersection getIntersection() {
        return null;
    }

    public List<LightPhase> getPhases() {
        return null;
    }

    public void passTime(double seconds) {

    }

    public void setPhase(int phaseIndex) {

    }

    public void reset() {

    }
}
