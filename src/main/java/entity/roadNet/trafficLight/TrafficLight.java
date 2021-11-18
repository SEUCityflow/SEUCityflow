package entity.roadNet.trafficLight;

import entity.roadNet.roadNet.Intersection;

import java.util.ArrayList;
import java.util.List;

public class TrafficLight {
    private Intersection intersection;
    private List<LightPhase> phases;
    private List<Integer> roadLinkIndices;
    private double remainDuration;
    private int curPhaseIndex;

    public TrafficLight() {
        phases = new ArrayList<LightPhase>();
        roadLinkIndices = new ArrayList<Integer>();
    }

    public void init(int initPhaseIndex) { // 设定当前信号灯 phase
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
        if (intersection.isVirtual()) {
            return;
        }
        remainDuration -= seconds;
        while (remainDuration <= 0.0) {
            curPhaseIndex = (curPhaseIndex + 1) % (int)phases.size();
            remainDuration += phases.get(curPhaseIndex).getTime();
        }
    }

    public void setPhase(int phaseIndex) {
        curPhaseIndex = phaseIndex;
    }

    public void reset() {
        init(0);
    }
}
