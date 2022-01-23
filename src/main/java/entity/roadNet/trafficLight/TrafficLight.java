package entity.roadNet.trafficLight;

import entity.roadNet.roadNet.Intersection;

import java.util.ArrayList;
import java.util.List;

public class TrafficLight {
    private Intersection intersection;
    private List<LightPhase> phases;
    private double remainDuration;
    private int curPhaseIndex;
    private List<Double> time;
    private double period;

    public TrafficLight() {
        phases = new ArrayList<>();
        time = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            time.add((double) 0);
        }
    }

    public void init(int initPhaseIndex) {
        if (intersection.isVirtual()) {
            return;
        }
        curPhaseIndex = initPhaseIndex;
        remainDuration = phases.get(initPhaseIndex).getTime();
    }

    public int getCurrentPhaseIndex() {
        return curPhaseIndex;
    }

    public LightPhase getCurrentPhase() {
        return phases.get(curPhaseIndex);
    }

    public Intersection getIntersection() {
        return intersection;
    }

    public List<LightPhase> getPhases() {
        return phases;
    }

    public void passTime(double seconds) { // 时间过了 seconds
        remainDuration -= seconds;
        while (remainDuration <= 0.0) {
            curPhaseIndex = (curPhaseIndex + 1) % phases.size();
            remainDuration += phases.get(curPhaseIndex).getTime();
        }
    }

    public double getExpectedWaitingTime(int p) {
        return (period - time.get(p)) * (period - time.get(p)) / period / 2;
    }

    public void setPhase(int phaseIndex) {
        curPhaseIndex = phaseIndex;
    }

    public void reset() {
        init(0);
    }

    public void setIntersection(Intersection intersection) {
        this.intersection = intersection;
    }

    public void setPhases(List<LightPhase> phases) {
        this.phases = phases;
    }

    public double getRemainDuration() {
        return remainDuration;
    }

    public void setRemainDuration(double remainDuration) {
        this.remainDuration = remainDuration;
    }

    public int getCurPhaseIndex() {
        return curPhaseIndex;
    }

    public void setCurPhaseIndex(int curPhaseIndex) {
        this.curPhaseIndex = curPhaseIndex;
    }

    public List<Double> getTime() {
        return time;
    }

    public void setTime(List<Double> time) {
        this.time = time;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }
}
