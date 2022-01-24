package entity.archive;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

class TrafficLightArchive {
    @JSONField(name = "trafficId", ordinal = 1)
    private String trafficId;
    @JSONField(name = "remainDuration", ordinal = 2)
    private double remainDuration;
    @JSONField(name = "curPhaseIndex", ordinal = 3)
    private int curPhaseIndex;
    @JSONField(name = "cumulateTimes", ordinal = 4)
    private List<Double> cumulateTimes;

    public String getTrafficId() {
        return trafficId;
    }

    public void setTrafficId(String trafficId) {
        this.trafficId = trafficId;
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

    public List<Double> getCumulateTimes() {
        return cumulateTimes;
    }

    public void setCumulateTimes(List<Double> cumulateTimes) {
        this.cumulateTimes = cumulateTimes;
    }
}
