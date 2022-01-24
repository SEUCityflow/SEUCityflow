package entity.roadNet.trafficLight;

import entity.roadNet.roadNet.Intersection;
import entity.roadNet.roadNet.Lane;
import entity.roadNet.roadNet.LaneLink;
import entity.roadNet.roadNet.RoadLink;

import java.util.*;

public class TrafficLight {
    private Intersection intersection;
    private List<LightPhase> phases;
    private double remainDuration;
    private int curPhaseIndex;
    private double[] time;
    private double period;
    private List<Double> cumulateTimes;

    public TrafficLight() {
        phases = new ArrayList<>();
        time = new double[3];
        cumulateTimes = new LinkedList<>();
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
            updateCumulateTime((curPhaseIndex + 1) % phases.size(), curPhaseIndex, phases.get(curPhaseIndex).getTime());
            curPhaseIndex = (curPhaseIndex + 1) % phases.size();
            remainDuration += phases.get(curPhaseIndex).getTime();
        }
    }

    public void updateCumulateTime(int newPhaseIndex, int lastPhaseIndex, double time) {
        List<Boolean> newRoadLinkAvailable = phases.get(newPhaseIndex).getRoadLinkAvailable();
        List<Boolean> lastRoadLinkAvailable = phases.get(lastPhaseIndex).getRoadLinkAvailable();
        for (int i = 0; i < newRoadLinkAvailable.size(); i++) {
            if (lastRoadLinkAvailable.get(i) != newRoadLinkAvailable.get(i)) {
                RoadLink roadLink = intersection.getRoadLinks().get(i);
                Set<Lane> set = new HashSet<>();
                for (LaneLink laneLink : roadLink.getLaneLinks()) {
                    set.add(laneLink.getStartLane());
                }
                for (Lane lane : set) {
                    lane.addPeriodTime(cumulateTimes.get(i));
                }
                cumulateTimes.set(i, (double) 0);
            } else {
                cumulateTimes.set(i, cumulateTimes.get(i) + time);
            }
        }
    }

    public double getExpectedWaitingTime(int p) {
        return (period - time[p]) * (period - time[p]) / period / 2;
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
        remainDuration = phases.get(curPhaseIndex).getTime();
    }

    public double[] getTime() {
        return time;
    }

    public void setTime(double[] time) {
        this.time = time;
    }

    public double getPeriod() {
        return period;
    }

    public void setPeriod(double period) {
        this.period = period;
    }

    public List<Double> getCumulateTimes() {
        return cumulateTimes;
    }

    public void setCumulateTimes(List<Double> cumulateTimes) {
        this.cumulateTimes = cumulateTimes;
    }
}
